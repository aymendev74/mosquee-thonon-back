package org.mosqueethonon.v1.dto.bulletin;

import lombok.Data;
import org.mosqueethonon.enums.NoteMatiereEnum;

@Data
public class BulletinMatiereDto {

    private Long idMatiere;
    private NoteMatiereEnum note;
    private String remarque;

}
