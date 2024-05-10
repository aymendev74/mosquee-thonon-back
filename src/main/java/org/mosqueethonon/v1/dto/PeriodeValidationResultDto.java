package org.mosqueethonon.v1.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PeriodeValidationResultDto {

    private PeriodeDto periode;
    private boolean success;
    private String errorCode;

}
