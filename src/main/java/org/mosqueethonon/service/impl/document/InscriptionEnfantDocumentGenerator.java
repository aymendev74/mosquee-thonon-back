package org.mosqueethonon.service.impl.document;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.document.DocumentMetadataEntity;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.enums.DocumentMetadataKey;
import org.mosqueethonon.enums.NiveauScolaireEnum;
import org.mosqueethonon.service.document.DocumentGenerator;
import org.mosqueethonon.utils.HashUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class InscriptionEnfantDocumentGenerator implements DocumentGenerator<InscriptionEnfantEntity> {

    @Override
    public String getCode() {
        return "INS-ENFANT-001";
    }

    @Override
    public String getPath() {
        return "INSCRIPTION-ENFANT";
    }

    @Override
    public String getTemplateName() {
        return "documents/ins-enfant-001";
    }

    @Override
    public String generateFileName(InscriptionEnfantEntity entity) {
        return "inscription-enfant-" + entity.getNoInscription() + ".pdf";
    }

    @Override
    public String getAnnee(InscriptionEnfantEntity entity) {
        if (entity.getDateInscription() == null) {
            return null;
        }
        return Integer.toString(entity.getDateInscription().getYear());
    }

    @Override
    public Long getIdUtilisateur(InscriptionEnfantEntity entity) {
        return entity.getIdUtilisateur();
    }

    @Override
    public Map<String, Object> buildTemplateVariables(InscriptionEnfantEntity entity) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("noInscription", entity.getNoInscription());
        variables.put("anneeScolaire", entity.getAnneeScolaire());
        variables.put("montantTotal", entity.getMontantTotal());
        variables.put("dateInscription", entity.getDateInscription());

        if (entity.getResponsableLegal() != null) {
            variables.put("nom", entity.getResponsableLegal().getNom());
            variables.put("prenom", entity.getResponsableLegal().getPrenom());
            variables.put("email", entity.getResponsableLegal().getEmail());
            variables.put("mobile", entity.getResponsableLegal().getMobile());
            variables.put("numeroEtRue", entity.getResponsableLegal().getNumeroEtRue());
            variables.put("codePostal", entity.getResponsableLegal().getCodePostal());
            variables.put("ville", entity.getResponsableLegal().getVille());
            variables.put("adherent", entity.getResponsableLegal().getAdherent());
            variables.put("nomAutre", entity.getResponsableLegal().getNomAutre());
            variables.put("prenomAutre", entity.getResponsableLegal().getPrenomAutre());
            variables.put("lienParente", entity.getResponsableLegal().getLienParente());
            variables.put("telephoneAutre", entity.getResponsableLegal().getTelephoneAutre());
            variables.put("autorisationAutonomie", entity.getResponsableLegal().getAutorisationAutonomie());
            variables.put("autorisationMedia", entity.getResponsableLegal().getAutorisationMedia());
        }

        if (entity.getEleves() != null) {
            List<Map<String, String>> elevesData = entity.getEleves().stream()
                    .sorted(Comparator.comparing(e -> (e.getNom() + e.getPrenom())))
                    .map(eleve -> {
                        Map<String, String> eleveMap = new HashMap<>();
                        eleveMap.put("nom", eleve.getNom());
                        eleveMap.put("prenom", eleve.getPrenom());
                        eleveMap.put("dateNaissance", eleve.getDateNaissance() != null ? eleve.getDateNaissance().toString() : "");
                        eleveMap.put("niveau", getLibelleNiveauScolaire(eleve.getNiveau()));
                        eleveMap.put("niveauInterne", eleve.getNiveauInterne() != null ? eleve.getNiveauInterne().name() : "");
                        return eleveMap;
                    })
                    .toList();
            variables.put("eleves", elevesData);
        }

        return variables;
    }

    @Override
    public String computeHash(InscriptionEnfantEntity entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getNoInscription());
        sb.append("|").append(entity.getMontantTotal());
        sb.append("|").append(entity.getAnneeScolaire());

        if (entity.getResponsableLegal() != null) {
            sb.append("|").append(entity.getResponsableLegal().getNom());
            sb.append("|").append(entity.getResponsableLegal().getPrenom());
            sb.append("|").append(entity.getResponsableLegal().getEmail());
            sb.append("|").append(entity.getResponsableLegal().getMobile());
            sb.append("|").append(entity.getResponsableLegal().getNumeroEtRue());
            sb.append("|").append(entity.getResponsableLegal().getCodePostal());
            sb.append("|").append(entity.getResponsableLegal().getVille());
            sb.append("|").append(entity.getResponsableLegal().getAdherent());
            sb.append("|").append(entity.getResponsableLegal().getNomAutre());
            sb.append("|").append(entity.getResponsableLegal().getPrenomAutre());
            sb.append("|").append(entity.getResponsableLegal().getLienParente());
            sb.append("|").append(entity.getResponsableLegal().getTelephoneAutre());
            sb.append("|").append(entity.getResponsableLegal().getAutorisationAutonomie());
            sb.append("|").append(entity.getResponsableLegal().getAutorisationMedia());
        }

        if (entity.getEleves() != null) {
            entity.getEleves().stream()
                    .sorted(Comparator.comparing(e -> (e.getNom() + e.getPrenom())))
                    .forEach(eleve -> {
                        sb.append("|").append(eleve.getNom());
                        sb.append("|").append(eleve.getPrenom());
                        sb.append("|").append(eleve.getDateNaissance());
                        sb.append("|").append(eleve.getNiveau());
                        sb.append("|").append(eleve.getNiveauInterne());
                    });
        }

        return HashUtils.sha256(sb.toString());
    }

    @Override
    public List<DocumentMetadataEntity> buildMetadata(InscriptionEnfantEntity entity) {
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

    private String getLibelleNiveauScolaire(NiveauScolaireEnum niveau) {
        if (niveau == null) return "";
        return switch (niveau) {
            case CP -> "CP";
            case CE1 -> "CE1";
            case CE2 -> "CE2";
            case CM1 -> "CM1";
            case CM2 -> "CM2";
            case COLLEGE_6EME -> "6ème";
            case COLLEGE_5EME -> "5ème";
            case COLLEGE_4EME -> "4ème";
            case COLLEGE_3EME -> "3ème";
            case LYCEE_2ND -> "2nd";
            case LYCEE_1ERE -> "1ère";
            case LYCEE_TERM -> "Terminal";
            case AUTRE -> "Autre";
        };
    }

}
