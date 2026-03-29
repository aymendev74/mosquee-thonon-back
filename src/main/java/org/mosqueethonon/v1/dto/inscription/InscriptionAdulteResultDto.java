package org.mosqueethonon.v1.dto.inscription;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

@Data
@Builder
public class InscriptionAdulteResultDto {

    private Boolean newlyCreatedAccount;
    private Boolean enabledAccount;

}
