package org.mosqueethonon.service.impl.document;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.document.DocumentMetadataEntity;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionAdulteEntity;
import org.mosqueethonon.enums.DocumentMetadataKey;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.service.document.DocumentGenerator;
import org.mosqueethonon.service.referentiel.TraductionService;
import org.mosqueethonon.utils.HashUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class InscriptionAdulteDocumentGenerator implements DocumentGenerator<InscriptionAdulteEntity> {

    private final TraductionService traductionService;

    @Override
    public String getCode() {
        return "INS-ADULTE-001";
    }

    @Override
    public String getPath() {
        return "INSCRIPTION-ADULTE";
    }

    @Override
    public String getTemplateName() {
        return "documents/ins-adulte-001";
    }

    @Override
    public String generateFileName(InscriptionAdulteEntity entity) {
        return "inscription-adulte-" + entity.getNoInscription() + ".pdf";
    }

    @Override
    public String getAnnee(InscriptionAdulteEntity entity) {
        return Integer.toString(entity.getDateInscription().getYear());
    }

    @Override
    public Long getIdUtilisateur(InscriptionAdulteEntity entity) {
        return entity.getIdUtilisateur();
    }

    @Override
    public Map<String, Object> buildTemplateVariables(InscriptionAdulteEntity entity) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("noInscription", entity.getNoInscription());
        variables.put("statut", entity.getStatut() != null ? entity.getStatut().name() : null);
        variables.put("dateInscription", entity.getDateInscription());
        variables.put("montantTotal", entity.getMontantTotal());
        variables.put("statutProfessionnel", entity.getStatutProfessionnel() != null ? entity.getStatutProfessionnel().name() : null);
        variables.put("anneeScolaire", entity.getAnneeScolaire());

        if (entity.getResponsableLegal() != null) {
            variables.put("nom", entity.getResponsableLegal().getNom());
            variables.put("prenom", entity.getResponsableLegal().getPrenom());
            variables.put("email", entity.getResponsableLegal().getEmail());
            variables.put("mobile", entity.getResponsableLegal().getMobile());
            variables.put("numeroEtRue", entity.getResponsableLegal().getNumeroEtRue());
            variables.put("codePostal", entity.getResponsableLegal().getCodePostal());
            variables.put("ville", entity.getResponsableLegal().getVille());
        }

        if (entity.getEleves() != null && !entity.getEleves().isEmpty()) {
            EleveEntity eleve = entity.getEleves().get(0);
            variables.put("dateNaissance", eleve.getDateNaissance());
            variables.put("sexe", eleve.getSexe() != null ? eleve.getSexe().name() : null);
            variables.put("niveauInterne", eleve.getNiveauInterne() != null ? eleve.getNiveauInterne().name() : null);
        }

        if (entity.getMatieres() != null) {
            List<String> matieres = entity.getMatieres().stream()
                    .map(m -> resolveLibelleMatiere(m.getMatiere().getCode().name()))
                    .collect(Collectors.toList());
            variables.put("matieres", matieres);
        }

        return variables;
    }

    private String resolveLibelleMatiere(String codeMatiere) {
        try {
            return this.traductionService.findTraductionByCleAndValeur("cdmaticode", codeMatiere).getFr();
        } catch (ResourceNotFoundException e) {
            return codeMatiere;
        }
    }

    @Override
    public String computeHash(InscriptionAdulteEntity entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getNoInscription());
        sb.append("|").append(entity.getStatut());
        sb.append("|").append(entity.getMontantTotal());
        sb.append("|").append(entity.getStatutProfessionnel());
        sb.append("|").append(entity.getAnneeScolaire());

        if (entity.getResponsableLegal() != null) {
            sb.append("|").append(entity.getResponsableLegal().getNom());
            sb.append("|").append(entity.getResponsableLegal().getPrenom());
            sb.append("|").append(entity.getResponsableLegal().getEmail());
            sb.append("|").append(entity.getResponsableLegal().getMobile());
            sb.append("|").append(entity.getResponsableLegal().getNumeroEtRue());
            sb.append("|").append(entity.getResponsableLegal().getCodePostal());
            sb.append("|").append(entity.getResponsableLegal().getVille());
        }

        if (entity.getEleves() != null && !entity.getEleves().isEmpty()) {
            EleveEntity eleve = entity.getEleves().get(0);
            sb.append("|").append(eleve.getDateNaissance());
            sb.append("|").append(eleve.getSexe());
            sb.append("|").append(eleve.getNiveauInterne());
        }

        if (entity.getMatieres() != null) {
            entity.getMatieres().stream()
                    .map(m -> m.getMatiere().getCode().name())
                    .sorted()
                    .forEach(m -> sb.append("|").append(m));
        }

        return HashUtils.sha256(sb.toString());
    }

    @Override
    public List<DocumentMetadataEntity> buildMetadata(InscriptionAdulteEntity entity) {
        List<DocumentMetadataEntity> metadata = new ArrayList<>();
        metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.ID_INSCRIPTION, String.valueOf(entity.getId())));
        metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.NO_INSCRIPTION, entity.getNoInscription()));
        if (entity.getResponsableLegal() != null) {
            metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.NOM, entity.getResponsableLegal().getNom()));
            metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.PRENOM, entity.getResponsableLegal().getPrenom()));
            metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.EMAIL, entity.getResponsableLegal().getEmail()));
        }
        if (entity.getIdUtilisateur() != null) {
            metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.ID_UTILISATEUR, String.valueOf(entity.getIdUtilisateur())));
        }
        if (entity.getAnneeScolaire() != null) {
            metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.ANNEE_SCOLAIRE, entity.getAnneeScolaire()));
        }
        return metadata;
    }

}
