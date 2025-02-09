package org.mosqueethonon.service.inscription;

import com.fasterxml.jackson.databind.JsonNode;
import org.mosqueethonon.v1.criterias.SearchEleveCriteria;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.dto.inscription.EleveEnrichedDto;

import java.util.List;

public interface EleveService {

    List<EleveDto> findElevesByCriteria(SearchEleveCriteria criteria);

    void patchEleves(JsonNode patchesNode);

    List<EleveEnrichedDto> findElevesEnrichedByIdClasse(Long idClasse);

    EleveDto findEleveById(Long idEleve);

}
