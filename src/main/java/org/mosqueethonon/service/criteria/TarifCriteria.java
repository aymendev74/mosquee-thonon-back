package org.mosqueethonon.service.criteria;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TarifCriteria {

    private String application;
    private String type;
    private Boolean adherent;
    private Integer nbEnfant;
    private LocalDate atDate;

}
