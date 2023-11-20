package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.TarifDto;

import java.util.List;

public interface TarifService {

    List<TarifDto> findAllTarifs();

}
