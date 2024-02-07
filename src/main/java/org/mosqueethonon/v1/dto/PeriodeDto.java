package org.mosqueethonon.v1.dto;

import lombok.Data;

@Data
public class PeriodeDto {

    private Long id;
    private String dateDebut;
    private String dateFin;
    private Integer nbMaxInscription;
    private SignatureDto signature;
    private String application;

}
