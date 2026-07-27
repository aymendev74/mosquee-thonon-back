package org.mosqueethonon.inscription.v1.dto;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.inscription.enums.StatutInscriptionEnum;

@Data
@Builder
public class InscriptionEnfantResultDto {

    private StatutInscriptionEnum statut;
    private Boolean newlyCreatedAccount;
    private Boolean enabledAccount;

}
