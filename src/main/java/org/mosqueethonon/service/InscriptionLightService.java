package org.mosqueethonon.service;

import org.mosqueethonon.v1.criterias.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionLightDto;

import java.util.List;

public interface InscriptionLightService {

    public List<InscriptionLightDto> findInscriptionsLightByCriteria(InscriptionCriteria criteria);


}
