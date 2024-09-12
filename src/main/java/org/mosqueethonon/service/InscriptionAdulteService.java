package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.InscriptionAdulteDto;
import org.mosqueethonon.v1.dto.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.InscriptionSaveCriteria;

public interface InscriptionAdulteService {

    public InscriptionAdulteDto createInscription(InscriptionAdulteDto inscription, InscriptionSaveCriteria criteria);

    public InscriptionAdulteDto updateInscription(Long id, InscriptionAdulteDto inscription, InscriptionSaveCriteria criteria);

    public InscriptionAdulteDto findInscriptionById(Long id);
}
