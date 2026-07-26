package org.mosqueethonon.bulletin.v1.dto;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.bulletin.enums.NoteMatiereEnum;

@Data
@Builder
public class BulletinMatiereDto {

    private MatiereEnum code;
    private NoteMatiereEnum note;
    private String remarque;

}
