package org.mosqueethonon.inscription.v1.dto;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.inscription.enums.StatutInscription;

@Data
@Builder
public class InscriptionEnfantResultDto {

    private StatutInscription statut;
    private Boolean newlyCreatedAccount;
    private Boolean enabledAccount;

}
