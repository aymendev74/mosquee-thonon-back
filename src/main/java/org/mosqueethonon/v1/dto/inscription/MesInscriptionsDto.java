package org.mosqueethonon.v1.dto.inscription;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MesInscriptionsDto {

    private List<InscriptionEnfantParAnneeScolaireDto> inscriptionsEnfants;
    private List<InscriptionAdulteParAnneeScolaireDto> inscriptionsAdultes;
}
