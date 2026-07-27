package org.mosqueethonon.inscription.v1.dto;

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
