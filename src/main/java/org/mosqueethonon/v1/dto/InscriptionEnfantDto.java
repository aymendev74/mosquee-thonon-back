package org.mosqueethonon.v1.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.configuration.APIDateFormats;
import org.mosqueethonon.entity.Signature;
import org.mosqueethonon.utils.StringUtils;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class InscriptionEnfantDto {

    private StatutInscription statut;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = APIDateFormats.DATE_TIME_FORMAT)
    private LocalDateTime dateInscription;
    private ResponsableLegalDto responsableLegal;
    private List<EleveDto> eleves;
    private String noInscription;
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
