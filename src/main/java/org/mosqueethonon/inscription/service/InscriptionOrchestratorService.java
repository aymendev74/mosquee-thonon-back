package org.mosqueethonon.inscription.service;

import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionSaveCriteria;

import java.util.List;
import java.util.Set;

public interface InscriptionOrchestratorService {

    //List<InscriptionEnfantDto> updateInscriptions(List<InscriptionEnfantDto> inscriptions);

    InscriptionEnfantDto updateInscription(Long id, InscriptionEnfantDto inscription, InscriptionSaveCriteria criteria);

    Set<Long> deleteInscriptions(Set<Long> ids);

    void deleteByIdUtilisateur(Long idUtilisateur);

}
