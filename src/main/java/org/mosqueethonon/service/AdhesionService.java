package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.AdhesionDto;
import org.mosqueethonon.v1.dto.AdhesionPatchDto;

import java.util.Set;

public interface AdhesionService {

    public AdhesionDto createAdhesion(AdhesionDto adhesiondto);

    public AdhesionDto updateAdhesion(Long id, AdhesionDto adhesiondto);

    public AdhesionDto findAdhesionById(Long id);

    public Set<Long> deleteAdhesions(Set<Long> ids);

    public Set<Long> patchAdhesions(AdhesionPatchDto adhesionPatchDto);

}
