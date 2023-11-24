package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.InscriptionInfosDto;
import org.mosqueethonon.v1.dto.TarifInscriptionDto;

public interface TarifCalculService {

    public TarifInscriptionDto calculTarifInscription(InscriptionInfosDto inscriptionInfos);

}
