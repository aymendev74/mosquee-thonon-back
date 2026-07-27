package org.mosqueethonon.inscription.v1.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MesInscriptionsDto {

    private List<InscriptionEnfantParAnneeScolaireDto> inscriptionsEnfants;
    private List<InscriptionAdulteParAnneeScolaireDto> inscriptionsAdultes;
}
