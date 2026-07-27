package org.mosqueethonon.adhesion.service;

import org.mosqueethonon.adhesion.v1.criteria.AdhesionCriteria;
import org.mosqueethonon.adhesion.v1.dto.AdhesionLightDto;

import java.util.List;

public interface AdhesionLightService {

    public List<AdhesionLightDto> findAdhesionsLightByCriteria(AdhesionCriteria criteria);

}
