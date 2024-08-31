package org.mosqueethonon.service;

import org.mosqueethonon.v1.criterias.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionEnfantLightDto;

import java.util.List;

public interface InscriptionEnfantLightService {

    public List<InscriptionEnfantLightDto> findInscriptionsEnfantLightByCriteria(InscriptionCriteria criteria);


}
