package org.mosqueethonon.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.service.document.DocumentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "/v1/documents")
@AllArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final SecurityContext securityContext;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping(path = "/{id}/contenu")
    public ResponseEntity<byte[]> getDocumentContent(@PathVariable("id") Long id) {
        DocumentEntity document = this.documentService.findById(id);
        this.checkAccess(document);
        byte[] content = this.documentService.getDocumentContent(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", document.getLibelle());
        headers.setContentLength(content.length);

        return ResponseEntity.ok().headers(headers).body(content);
    }

    private void checkAccess(DocumentEntity document) {
        // L'utilisateur lié au document peut toujours y accéder
        String username = this.securityContext.getUser();
        if (username != null) {
            Long currentUserId = this.utilisateurRepository.findByUsername(username)
                    .map(u -> u.getId())
                    .orElse(null);
            if (currentUserId != null && currentUserId.equals(document.getIdUtilisateur())) {
                return;
            }
        }

        // Vérification des permissions selon le type de document
        String documentCode = document.getCode();

        if ("ADHESION-001".equals(documentCode)) {
            // Documents adhésion : accessible aux TRESORIER et ADMIN
            if (!this.securityContext.hasRole("ROLE_TRESORIER") && !this.securityContext.hasRole("ROLE_ADMIN")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à ce document");
            }
        } else if ("INS-ADULTE-001".equals(documentCode) || "INS-ENFANT-001".equals(documentCode)) {
            // Documents inscription : accessible aux ENSEIGNANT et ADMIN
            if (!this.securityContext.hasRole("ROLE_ENSEIGNANT") && !this.securityContext.hasRole("ROLE_ADMIN")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à ce document");
            }
        } else if ("BULLETIN-001".equals(documentCode)) {
            // Documents bulletin : accessible aux ENSEIGNANT et ADMIN
            if (!this.securityContext.hasRole("ROLE_ENSEIGNANT") && !this.securityContext.hasRole("ROLE_ADMIN")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à ce document");
            }
        } else {
            // Type de document inconnu ou non géré : accès refusé
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à ce document");
        }
    }

}
