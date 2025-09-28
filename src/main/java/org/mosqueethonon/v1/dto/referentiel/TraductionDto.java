package org.mosqueethonon.v1.dto.referentiel;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TraductionDto {

    private String code;
    private String fr;

}
