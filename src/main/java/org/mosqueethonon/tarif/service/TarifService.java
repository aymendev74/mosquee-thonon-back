package org.mosqueethonon.tarif.service;

import org.mosqueethonon.tarif.criteria.TarifCriteria;
import org.mosqueethonon.tarif.v1.dto.TarifDto;

import java.util.List;

public interface TarifService {

    List<TarifDto> findTarifByCriteria(TarifCriteria criteria);

}
