package org.mosqueethonon.service.inscription;

import org.mosqueethonon.v1.criterias.SearchEleveCriteria;
import org.mosqueethonon.v1.dto.inscription.EleveDto;

import java.util.List;

public interface EleveService {

    List<EleveDto> findElevesByCriteria(SearchEleveCriteria criteria);

}
