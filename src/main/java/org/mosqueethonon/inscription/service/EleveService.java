package org.mosqueethonon.inscription.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.mosqueethonon.inscription.v1.criteria.SearchEleveCriteria;
import org.mosqueethonon.inscription.v1.dto.EleveDto;
import org.mosqueethonon.inscription.v1.dto.EleveEnrichedDto;

import java.util.List;

public interface EleveService {

    List<EleveDto> findElevesByCriteria(SearchEleveCriteria criteria);

    void patchEleves(JsonNode patchesNode);

    List<EleveEnrichedDto> findElevesEnrichedByIdClasse(Long idClasse);

    EleveDto findEleveById(Long idEleve);

}
