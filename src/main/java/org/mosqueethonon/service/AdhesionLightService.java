package org.mosqueethonon.service;

import org.mosqueethonon.service.criteria.AdhesionCriteria;
import org.mosqueethonon.v1.dto.AdhesionLightDto;

import java.util.List;

public interface AdhesionLightService {

    public List<AdhesionLightDto> findAdhesionsLightByCriteria(AdhesionCriteria criteria);

}
