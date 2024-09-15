package org.mosqueethonon.service.referentiel;

import org.mosqueethonon.service.criteria.TarifCriteria;
import org.mosqueethonon.v1.dto.referentiel.TarifDto;

import java.util.List;

public interface TarifService {

    List<TarifDto> findTarifByCriteria(TarifCriteria criteria);

}
