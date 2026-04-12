package org.mosqueethonon.service.impl.document;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.adhesion.AdhesionEntity;
import org.mosqueethonon.entity.document.DocumentMetadataEntity;
import org.mosqueethonon.enums.DocumentMetadataKey;
import org.mosqueethonon.service.document.DocumentGenerator;
import org.mosqueethonon.utils.HashUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class AdhesionDocumentGenerator implements DocumentGenerator<AdhesionEntity> {

    @Override
    public String getCode() {
        return "ADHESION-001";
    }

    @Override
    public String getPath() {
        return "ADHESION";
    }

    @Override
    public String getTemplateName() {
        return "documents/adhesion-001";
    }

    @Override
    public String generateFileName(AdhesionEntity entity) {
        return "adhesion-" + entity.getId() + ".pdf";
    }

    @Override
    public String getAnnee(AdhesionEntity entity) {
        if (entity.getDateInscription() == null) {
            return null;
        }
        return Integer.toString(entity.getDateInscription().getYear());
    }

    @Override
    public Long getIdUtilisateur(AdhesionEntity entity) {
        return null;
    }

    @Override
    public Map<String, Object> buildTemplateVariables(AdhesionEntity entity) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("noMembre", entity.getNoMembre());
        variables.put("nom", entity.getNom());
        variables.put("prenom", entity.getPrenom());
        variables.put("email", entity.getEmail());
        variables.put("mobile", entity.getMobile());
        variables.put("numeroEtRue", entity.getNumeroEtRue());
        variables.put("codePostal", entity.getCodePostal());
        variables.put("ville", entity.getVille());
        variables.put("soussigneLibelle", getSoussigneLibelle(entity.getTitre()));
        variables.put("etatCivil", getEtatCivil(entity.getTitre()));
        variables.put("montant", getMontant(entity));
        return variables;
    }

    @Override
    public String computeHash(AdhesionEntity entity) {
        StringBuilder sb = new StringBuilder();
        sb.append(entity.getId());
        sb.append("|").append(entity.getNoMembre());
        sb.append("|").append(entity.getTitre());
        sb.append("|").append(entity.getNom());
        sb.append("|").append(entity.getPrenom());
        sb.append("|").append(entity.getEmail());
        sb.append("|").append(entity.getMobile());
        sb.append("|").append(entity.getNumeroEtRue());
        sb.append("|").append(entity.getCodePostal());
        sb.append("|").append(entity.getVille());
        sb.append("|").append(getMontant(entity));
        return HashUtils.sha256(sb.toString());
    }

    @Override
    public List<DocumentMetadataEntity> buildMetadata(AdhesionEntity entity) {
        List<DocumentMetadataEntity> metadata = new ArrayList<>();
        metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.ID_ADHESION, String.valueOf(entity.getId())));
        metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.NOM, entity.getNom()));
        metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.PRENOM, entity.getPrenom()));
        metadata.add(new DocumentMetadataEntity(DocumentMetadataKey.EMAIL, entity.getEmail()));
        return metadata;
    }

    private String getEtatCivil(String titre) {
        return "M".equals(titre) ? "Monsieur" : "Madame";
    }

    private String getSoussigneLibelle(String titre) {
        return "M".equals(titre) ? "Je soussigné" : "Je soussignée";
    }

    private BigDecimal getMontant(AdhesionEntity entity) {
        return entity.getMontant() != null ? entity.getMontant() : entity.getMontantAutre();
    }
}
