package org.mosqueethonon.v1.dto.referentiel;

import lombok.Data;
import org.mosqueethonon.entity.audit.Signature;

import java.math.BigDecimal;

@Data
public class TarifDto {

    private Long id;
    private PeriodeInfoDto periode;
    private String type;
    private Boolean adherent;
    private Integer nbEnfant;
    private BigDecimal montant;
    private Signature signature;

}
