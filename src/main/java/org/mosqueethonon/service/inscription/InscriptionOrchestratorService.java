package org.mosqueethonon.service.inscription;

import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;

import java.util.List;
import java.util.Set;

public interface InscriptionOrchestratorService {

    //List<InscriptionEnfantDto> updateInscriptions(List<InscriptionEnfantDto> inscriptions);

    InscriptionEnfantDto updateInscription(Long id, InscriptionEnfantDto inscription, InscriptionSaveCriteria criteria);

    Set<Long> deleteInscriptions(Set<Long> ids);

}
