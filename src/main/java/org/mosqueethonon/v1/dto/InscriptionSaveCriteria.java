package org.mosqueethonon.v1.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InscriptionSaveCriteria {

    private Boolean sendMailConfirmation;

}
