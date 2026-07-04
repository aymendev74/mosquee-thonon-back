package org.mosqueethonon.service.impl.document;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.configuration.security.ApplicationConfiguration;
import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.entity.document.DocumentMetadataEntity;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.DocumentRepository;
import org.mosqueethonon.service.document.DocumentGenerator;
import org.mosqueethonon.service.document.DocumentService;
import org.mosqueethonon.service.document.PdfGeneratorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final ApplicationConfiguration applicationConfiguration;

    @Override
    public <T> DocumentEntity generateOrUpdateDocument(DocumentGenerator<T> generator, T entity) {
        String newHash = generator.computeHash(entity);
        String fileName = generator.generateFileName(entity);
        String annee = generator.getAnnee(entity);

        String relativePath = buildRelativePath(generator.getPath(), annee, fileName);

        Optional<DocumentEntity> existingDoc = this.findExistingDocument(relativePath);

        if (existingDoc.isPresent()) {
            DocumentEntity doc = existingDoc.get();
            if (newHash.equals(doc.getHash())) {
                log.info("Le hash n'a pas changé pour le document {}, pas de regénération", doc.getChemin());
                return doc;
            }
            log.info("Le hash a changé pour le document {}, regénération en cours", doc.getChemin());
            return this.regenerateDocument(doc, generator, entity, newHash, relativePath);
        }
        log.info("Le document {} n'existe pas, création en cours", relativePath);
        return this.createNewDocument(generator, entity, newHash, relativePath, fileName, annee);
    }

    @Override
    public byte[] getDocumentContent(Long documentId) {
        DocumentEntity document = this.findById(documentId);
        Path filePath = resolveAbsolutePath(document.getChemin());
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Erreur lors de la lecture du document : {}", filePath, e);
            throw new RuntimeException("Impossible de lire le document", e);
        }
    }

    @Override
    public DocumentEntity findById(Long documentId) {
        return this.documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé avec l'id : " + documentId));
    }

    private <T> Optional<DocumentEntity> findExistingDocument(String relativePath) {
        return this.documentRepository.findByChemin(relativePath);
    }

    private <T> DocumentEntity regenerateDocument(DocumentEntity doc, DocumentGenerator<T> generator, T entity, String newHash, String relativePath) {
        Map<String, Object> variables = generator.buildTemplateVariables(entity);
        byte[] pdfContent = this.pdfGeneratorService.generatePdf(generator.getTemplateName(), variables);
        this.writeFile(relativePath, pdfContent);

        doc.setHash(newHash);
        doc.getMetadonnees().clear();
        doc.getMetadonnees().addAll(generator.buildMetadata(entity));
        return this.documentRepository.save(doc);
    }

    private <T> DocumentEntity createNewDocument(DocumentGenerator<T> generator, T entity, String hash, String relativePath, String fileName, String annee) {
        Map<String, Object> variables = generator.buildTemplateVariables(entity);
        byte[] pdfContent = this.pdfGeneratorService.generatePdf(generator.getTemplateName(), variables);
        this.writeFile(relativePath, pdfContent);

        DocumentEntity document = new DocumentEntity();
        document.setLibelle(fileName);
        document.setCode(generator.getCode());
        document.setChemin(relativePath);
        document.setHash(hash);
        document.setAnnee(annee);
        document.setIdUtilisateur(generator.getIdUtilisateur(entity));

        List<DocumentMetadataEntity> metadata = generator.buildMetadata(entity);
        document.getMetadonnees().addAll(metadata);

        return this.documentRepository.save(document);
    }

    private void writeFile(String relativePath, byte[] content) {
        Path filePath = resolveAbsolutePath(relativePath);
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content);
            log.info("Document PDF écrit sur le filesystem : {}", filePath);
        } catch (IOException e) {
            log.error("Erreur lors de l'écriture du document PDF : {}", filePath, e);
            throw new RuntimeException("Impossible d'écrire le document PDF", e);
        }
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {
        DocumentEntity doc = this.documentRepository.findById(documentId).orElse(null);
        if (doc == null) {
            log.warn("Document non trouvé pour suppression, id = {}", documentId);
            return;
        }
        Path filePath = resolveAbsolutePath(doc.getChemin());
        this.documentRepository.delete(doc); // suppression en base dans la transaction

        // Suppression physique planifiée après commit (irréversible, donc hors transaction)
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        boolean deleted = Files.deleteIfExists(filePath);
                        if (deleted) log.info("Fichier physique supprimé : {}", filePath);
                        else log.warn("Fichier physique introuvable lors de la suppression : {}", filePath);
                    } catch (IOException e) {
                        log.error("Erreur lors de la suppression physique du fichier : {}", filePath, e);
                    }
                }
            }
        );
    }

    private Path resolveAbsolutePath(String relativePath) {
        return Paths.get(this.applicationConfiguration.getDocuments().getBasePath()).resolve(relativePath);
    }

    private String buildRelativePath(String type, String annee, String fileName) {
        if (annee != null && !annee.isBlank()) {
            return type + "/" + annee + "/" + fileName;
        }
        return type + "/" + fileName;
    }

}
