package org.mosqueethonon.v1.dto.inscription;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.utils.StringUtils;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class InscriptionEnfantDto {

    private StatutInscription statut;
    private ResponsableLegalDto responsableLegal;
    private List<EleveDto> eleves;
    private Integer noPositionAttente;
    private String anneeScolaire;
    private BigDecimal montantTotal;

    public void normalize() {
        if(responsableLegal != null) {
            responsableLegal.setNom(StringUtils.normalize(responsableLegal.getNom()));
            responsableLegal.setPrenom(StringUtils.normalize(responsableLegal.getPrenom()));
            responsableLegal.setPrenomAutre(StringUtils.normalize(responsableLegal.getPrenomAutre()));
            responsableLegal.setNomAutre(StringUtils.normalize(responsableLegal.getNomAutre()));
        }
        if(!CollectionUtils.isEmpty(eleves)) {
            eleves.stream().forEach(eleve -> {
                eleve.setNom(StringUtils.normalize(eleve.getNom()));
                eleve.setPrenom(StringUtils.normalize(eleve.getPrenom()));
            });
        }
    }
}
