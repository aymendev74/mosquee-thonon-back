package org.mosqueethonon.adhesion.v1.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdhesionSaveCriteria {

    private Boolean sendMailConfirmation;

}
