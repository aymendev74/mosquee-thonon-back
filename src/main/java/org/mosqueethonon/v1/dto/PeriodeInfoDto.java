package org.mosqueethonon.v1.dto;

import lombok.Data;

@Data
public class PeriodeInfoDto extends PeriodeDto {

    private Boolean existInscription;
    private Boolean active;

}
