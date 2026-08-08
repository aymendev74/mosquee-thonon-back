package org.mosqueethonon.inscription.v1.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.common.util.StringUtils;
import org.mosqueethonon.inscription.enums.StatutInscriptionEnum;
import org.mosqueethonon.paiement.v1.dto.SituationPaiementDto;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class InscriptionEnfantDto {

    private Long id;
    private Long idDocument;
    private StatutInscriptionEnum statut;
    private ResponsableLegalDto responsableLegal;
    private List<EleveDto> eleves;
    private Integer noPositionAttente;
    private BigDecimal montantTotal;
    private String anneeScolaire;
    /**
     * En lecture seule, comme {@link #montantTotal} : peuplé par {@code findInscriptionById} pour
     * éviter au front un second appel quand il ouvre une inscription. Ignoré à l'entrée.
     */
    private SituationPaiementDto situationPaiement;

    public void normalize() {
        if(responsableLegal != null) {
            responsableLegal.setNom(StringUtils.normalize(responsableLegal.getNom()));
            responsableLegal.setPrenom(StringUtils.normalize(responsableLegal.getPrenom()));
            responsableLegal.setPrenomAutre(StringUtils.normalize(responsableLegal.getPrenomAutre()));
            responsableLegal.setNomAutre(StringUtils.normalize(responsableLegal.getNomAutre()));
            responsableLegal.setEmail(responsableLegal.getEmail().trim().toLowerCase());
        }
        if(!CollectionUtils.isEmpty(eleves)) {
            eleves.forEach(eleve -> {
                eleve.setNom(StringUtils.normalize(eleve.getNom()));
                eleve.setPrenom(StringUtils.normalize(eleve.getPrenom()));
            });
        }
    }
}
