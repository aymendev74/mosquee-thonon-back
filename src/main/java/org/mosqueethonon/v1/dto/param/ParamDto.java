package org.mosqueethonon.v1.dto.param;

import lombok.Data;
import org.mosqueethonon.enums.ParamNameEnum;

@Data
public class ParamDto {

    private ParamNameEnum name;
    private String value;

}
