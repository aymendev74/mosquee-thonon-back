package org.mosqueethonon.v1.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TarifInscriptionDto {

    private Long idTariBase;
    private BigDecimal tarifBase;
    private Long idTariEleve;
    private BigDecimal tarifEleve;
    private boolean listeAttente;

}
