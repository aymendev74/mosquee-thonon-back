package org.mosqueethonon.service.adhesion;

import com.fasterxml.jackson.databind.JsonNode;
import org.mosqueethonon.v1.dto.adhesion.AdhesionDto;
import org.mosqueethonon.v1.dto.adhesion.AdhesionSaveCriteria;

import java.util.Set;

public interface AdhesionService {

    public AdhesionDto createAdhesion(AdhesionDto adhesiondto);

    public AdhesionDto updateAdhesion(Long id, AdhesionDto adhesiondto, AdhesionSaveCriteria saveCriteria);

    public AdhesionDto findAdhesionById(Long id);

    public Set<Long> deleteAdhesions(Set<Long> ids);

    public Set<Long> patchAdhesions(JsonNode patchesNode);

}
