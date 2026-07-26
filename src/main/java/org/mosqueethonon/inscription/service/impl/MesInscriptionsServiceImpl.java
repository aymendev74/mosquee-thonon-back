package org.mosqueethonon.inscription.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.inscription.service.InscriptionAdulteService;
import org.mosqueethonon.inscription.service.InscriptionEnfantService;
import org.mosqueethonon.inscription.service.MesInscriptionsService;
import org.mosqueethonon.inscription.v1.dto.InscriptionAdulteParAnneeScolaireDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantParAnneeScolaireDto;
import org.mosqueethonon.inscription.v1.dto.MesInscriptionsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class MesInscriptionsServiceImpl implements MesInscriptionsService {

    private InscriptionEnfantService inscriptionEnfantService;
    private InscriptionAdulteService inscriptionAdulteService;

    @Override
    public MesInscriptionsDto findMesInscriptions() {
        // Récupération des inscriptions enfants
        List<InscriptionEnfantParAnneeScolaireDto> inscriptionsEnfants = this.inscriptionEnfantService.findInscriptionsByUtilisateurConnecte();

        // Récupération des inscriptions adultes
        List<InscriptionAdulteParAnneeScolaireDto> inscriptionsAdultes = this.inscriptionAdulteService.findInscriptionsByUtilisateurConnecte();

        return MesInscriptionsDto.builder()
                .inscriptionsEnfants(inscriptionsEnfants)
                .inscriptionsAdultes(inscriptionsAdultes)
                .build();
    }
}
