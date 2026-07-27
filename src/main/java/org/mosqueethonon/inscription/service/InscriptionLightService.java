package org.mosqueethonon.inscription.service;

import org.mosqueethonon.inscription.v1.criteria.InscriptionCriteria;
import org.mosqueethonon.inscription.v1.dto.InscriptionLightDto;

import java.util.List;

public interface InscriptionLightService {

    public List<InscriptionLightDto> findInscriptionsEnfantLightByCriteria(InscriptionCriteria criteria);


}
