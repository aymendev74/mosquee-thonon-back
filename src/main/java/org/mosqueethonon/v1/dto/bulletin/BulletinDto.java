package org.mosqueethonon.v1.dto.bulletin;

import lombok.Data;

import java.util.List;

@Data
public class BulletinDto {

    private Long idEleve;
    private String appreciation;
    private Integer nbAbsences;
    private Integer mois;
    private Integer annee;
    private List<BulletinMatiereDto> bulletinMatieres;

}
