package org.mosqueethonon.v1.dto;

import lombok.Data;
import org.mosqueethonon.entity.Signature;

@Data
public class PeriodeDto {

    private Long id;
    private String dateDebut;
    private String dateFin;
    private Integer nbMaxInscription;
    private Signature signature;

}