package org.mosqueethonon.v1.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TarifInscriptionDto {

    private BigDecimal tarifBase;
    private BigDecimal tarifEleve;

}
