package org.mosqueethonon.referentiel.v1.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TraductionDto {

    private String code;
    private String fr;

}
