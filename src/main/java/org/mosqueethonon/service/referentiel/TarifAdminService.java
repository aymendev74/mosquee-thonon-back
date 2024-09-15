package org.mosqueethonon.service.referentiel;

import org.mosqueethonon.v1.dto.referentiel.InfoTarifDto;

public interface TarifAdminService {

    InfoTarifDto findInfoTarifByPeriode(Long idPeriode);

    InfoTarifDto saveInfoTarif(InfoTarifDto infoTarifDto);

}
