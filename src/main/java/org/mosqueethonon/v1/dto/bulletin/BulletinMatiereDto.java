package org.mosqueethonon.v1.dto.bulletin;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.NoteMatiereEnum;

@Data
@Builder
public class BulletinMatiereDto {

    private MatiereEnum code;
    private NoteMatiereEnum note;
    private String remarque;

}
