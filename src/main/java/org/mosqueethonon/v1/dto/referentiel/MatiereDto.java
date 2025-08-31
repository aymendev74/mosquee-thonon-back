package org.mosqueethonon.v1.dto.referentiel;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.enums.MatiereEnum;

@Data
@Builder
public class MatiereDto {

    private Long id;
    private MatiereEnum code;
    private String libelle;

}
