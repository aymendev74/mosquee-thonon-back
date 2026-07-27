package org.mosqueethonon.inscription.v1.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.mosqueethonon.inscription.enums.StatutInscriptionEnum;

import java.math.BigDecimal;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
public class InscriptionEnfantParAnneeScolaireDto {

    private Integer anneeDebut;
    private Integer anneeFin;
    private StatutInscriptionEnum statut;
    private BigDecimal montantTotal;
    private ResponsableLegalDto responsableLegal;
    private List<EleveDto> eleves;
    private String noInscription;
    private Long idDocument;

}
