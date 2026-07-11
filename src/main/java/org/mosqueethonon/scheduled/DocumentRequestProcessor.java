package org.mosqueethonon.scheduled;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.entity.adhesion.AdhesionEntity;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.repository.BulletinRepository;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.repository.InscriptionAdulteRepository;
import org.mosqueethonon.repository.InscriptionEnfantRepository;
import org.mosqueethonon.service.document.DocumentService;
import org.mosqueethonon.service.impl.document.AdhesionDocumentGenerator;
import org.mosqueethonon.service.impl.document.BulletinDocumentGenerator;
import org.mosqueethonon.service.impl.document.InscriptionAdulteDocumentGenerator;
import org.mosqueethonon.service.impl.document.InscriptionEnfantDocumentGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class DocumentRequestProcessor {

    private final DocumentService documentService;
    private final DocumentRequestRepository documentRequestRepository;
    private final InscriptionAdulteDocumentGenerator inscriptionAdulteDocumentGenerator;
    private final InscriptionAdulteRepository inscriptionAdulteRepository;
    private final InscriptionEnfantDocumentGenerator inscriptionEnfantDocumentGenerator;
    private final InscriptionEnfantRepository inscriptionEnfantRepository;
    private final AdhesionDocumentGenerator adhesionDocumentGenerator;
    private final AdhesionRepository adhesionRepository;
    private final BulletinDocumentGenerator bulletinDocumentGenerator;
    private final BulletinRepository bulletinRepository;

    /**
     * Traite une demande de génération de document en participant à la transaction appelante (REQUIRED).
     * Retourne true si le document a été traité avec succès (statut COMPLETED), false sinon.
     * Le lock SELECT FOR UPDATE acquis en amont par le job reste ainsi maintenu jusqu'au commit
     * de la transaction englobante, garantissant l'isolation en environnement multi-instances.
     */
    @Transactional
    public boolean processDocumentRequest(DocumentRequestEntity request) {
        try {
            log.info("Traitement en cours de la demande de génération du document {}", request.getId());

            switch (request.getType()) {
                case INSCRIPTION_ADULTE:
                    processInscriptionAdulteRequest(request);
                    break;
                case INSCRIPTION_ENFANT:
                    processInscriptionEnfantRequest(request);
                    break;
                case ADHESION:
                    processAdhesionRequest(request);
                    break;
                case BULLETIN:
                    processBulletinRequest(request);
                    break;
                default:
                    throw new IllegalStateException("Type de demande de document non géré : " + request.getType());
            }

            return true;

        } catch (Exception e) {
            log.error("Erreur lors du traitement de la demande de document {} : ", request.getId(), e);
            request.setStatut(DocumentRequestStatut.ERROR);
            request.setErrorMessage(e.getMessage());
            documentRequestRepository.save(request);
            return false;
        }
    }

    private void processInscriptionEnfantRequest(DocumentRequestEntity request) {
        InscriptionEnfantEntity inscription = inscriptionEnfantRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Inscription enfant non trouvée avec l'ID : " + request.getBusinessId()));

        DocumentEntity document = documentService.generateOrUpdateDocument(inscriptionEnfantDocumentGenerator, inscription);

        completeDocumentRequest(request, document);
    }

    private void processAdhesionRequest(DocumentRequestEntity request) {
        AdhesionEntity adhesion = adhesionRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Adhésion non trouvée avec l'ID : " + request.getBusinessId()));

        DocumentEntity document = documentService.generateOrUpdateDocument(adhesionDocumentGenerator, adhesion);

        completeDocumentRequest(request, document);
    }

    private void processBulletinRequest(DocumentRequestEntity request) {
        BulletinEntity bulletin = bulletinRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Bulletin non trouvé avec l'ID : " + request.getBusinessId()));

        DocumentEntity document = documentService.generateOrUpdateDocument(bulletinDocumentGenerator, bulletin);

        completeDocumentRequest(request, document);
    }

    private void processInscriptionAdulteRequest(DocumentRequestEntity request) {
        InscriptionAdulteEntity inscription = inscriptionAdulteRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new RuntimeException("Inscription adulte non trouvée avec l'ID : " + request.getBusinessId()));

        DocumentEntity document = documentService.generateOrUpdateDocument(inscriptionAdulteDocumentGenerator, inscription);

        completeDocumentRequest(request, document);
    }

    private void completeDocumentRequest(DocumentRequestEntity request, DocumentEntity document) {
        request.setDocumentPath(document.getChemin());
        request.setDocumentCode(document.getCode());
        request.setStatut(DocumentRequestStatut.COMPLETED);
        documentRequestRepository.save(request);

        log.info("Traitement terminé pour la demande de génération du document {} - chemin : {}", request.getId(), document.getChemin());
    }

}
