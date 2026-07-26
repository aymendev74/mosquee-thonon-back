package org.mosqueethonon.referentiel.v1.dto;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.referentiel.enums.MatiereEnum;

@Data
@Builder
public class MatiereDto {

    private Long id;
    private MatiereEnum code;
    private String libelle;

}
