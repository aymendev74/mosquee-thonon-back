package org.mosqueethonon.v1.dto.inscription;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.math.BigDecimal;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class InscriptionParAnneeScolaireDto {

    private Integer anneeDebut;
    private Integer anneeFin;
    private StatutInscription statut;
    private BigDecimal montantTotal;
    private ResponsableLegalDto responsableLegal;
    private List<EleveAvecAutorisationsDto> eleves;
    private String noInscription;

}
