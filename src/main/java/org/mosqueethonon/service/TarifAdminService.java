package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.InfoTarifDto;

public interface TarifAdminService {

    InfoTarifDto findInfoTarifByPeriode(Long idPeriode);

    InfoTarifDto saveInfoTarif(InfoTarifDto infoTarifDto);

}
