package org.mosqueethonon.inscription.v1.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InscriptionSaveCriteria {

    private Boolean sendMailConfirmation;

}
