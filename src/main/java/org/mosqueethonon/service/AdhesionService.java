package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.AdhesionDto;

import java.util.Set;

public interface AdhesionService {

    public AdhesionDto saveAdhesion(AdhesionDto adhesionEntity);

    public AdhesionDto findAdhesionById(Long id);

    public Set<Long> deleteAdhesions(Set<Long> ids);

    public Set<Long> validateAdhesions(Set<Long> ids);

}
