package org.mosqueethonon.service;

import org.mosqueethonon.service.criteria.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionLightDto;

import java.util.List;

public interface InscriptionLightService {

    public List<InscriptionLightDto> findInscriptionsLightByCriteria(InscriptionCriteria criteria);


}
