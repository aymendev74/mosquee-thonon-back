package org.mosqueethonon.tarif.service;

import org.mosqueethonon.tarif.v1.dto.InfoTarifDto;

public interface TarifAdminService {

    InfoTarifDto findInfoTarifByPeriode(Long idPeriode);

    InfoTarifDto saveInfoTarif(InfoTarifDto infoTarifDto);

}
