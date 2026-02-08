package org.mosqueethonon.v1.dto.inscription;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EleveAvecAutorisationsDto extends EleveDto {

    private Boolean autorisationAutonomie;
    private Boolean autorisationMedia;

}
