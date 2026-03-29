package org.mosqueethonon.service.impl.inscription;

import lombok.AllArgsConstructor;
import org.mosqueethonon.service.inscription.InscriptionAdulteService;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.service.inscription.MesInscriptionsService;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteParAnneeScolaireDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantParAnneeScolaireDto;
import org.mosqueethonon.v1.dto.inscription.MesInscriptionsDto;
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
