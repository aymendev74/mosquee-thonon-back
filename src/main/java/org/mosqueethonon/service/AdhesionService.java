package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.AdhesionDto;

import java.util.List;

public interface AdhesionService {

    public AdhesionDto saveAdhesion(AdhesionDto adhesionEntity);

    public AdhesionDto findAdhesionById(Long id);

    public List<Long> deleteAdhesions(List<Long> ids);

    public List<Long> validateInscriptions(List<Long> ids);

}
