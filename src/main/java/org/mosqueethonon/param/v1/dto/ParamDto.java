package org.mosqueethonon.param.v1.dto;

import lombok.Data;
import org.mosqueethonon.param.enums.ParamNameEnum;

@Data
public class ParamDto {

    private ParamNameEnum name;
    private String value;

}
