package org.mosqueethonon.v1.dto.adhesion;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdhesionSaveCriteria {

    private Boolean sendMailConfirmation;

}
