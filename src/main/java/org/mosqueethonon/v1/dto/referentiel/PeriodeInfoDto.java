package org.mosqueethonon.v1.dto.referentiel;

import lombok.Data;

@Data
public class PeriodeInfoDto extends PeriodeDto {

    private Long id;
    private Boolean existInscription;
    private Boolean active;

}
