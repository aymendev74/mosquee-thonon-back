package org.mosqueethonon.service.adhesion;

import org.mosqueethonon.v1.criterias.AdhesionCriteria;
import org.mosqueethonon.v1.dto.adhesion.AdhesionLightDto;

import java.util.List;

public interface AdhesionLightService {

    public List<AdhesionLightDto> findAdhesionsLightByCriteria(AdhesionCriteria criteria);

}
