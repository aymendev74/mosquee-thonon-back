package org.mosqueethonon.v1.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TarifInscriptionAdulteDto {

    private Long idTari;
    private BigDecimal tarif;

}
