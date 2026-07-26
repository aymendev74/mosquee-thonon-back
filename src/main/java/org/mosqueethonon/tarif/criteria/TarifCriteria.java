package org.mosqueethonon.tarif.criteria;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.tarif.enums.TypeTarifEnum;

import java.time.LocalDate;

@Data
@Builder
public class TarifCriteria {

    private String application;
    private TypeTarifEnum type;
    private Boolean adherent;
    private Integer nbEnfant;
    private LocalDate atDate;

}
