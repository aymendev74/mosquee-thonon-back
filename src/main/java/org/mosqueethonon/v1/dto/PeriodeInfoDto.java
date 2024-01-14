package org.mosqueethonon.v1.dto;

import lombok.Data;

@Data
public class PeriodeInfoDto {

    private Long id;
    private String dateDebut;
    private String dateFin;
    private Integer nbMaxInscription;
    private Boolean existInscription;

}
