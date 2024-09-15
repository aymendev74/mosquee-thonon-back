package org.mosqueethonon.v1.dto.referentiel;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TarifInscriptionAdulteDto {

    private Long idTari;
    private BigDecimal tarif;

}
