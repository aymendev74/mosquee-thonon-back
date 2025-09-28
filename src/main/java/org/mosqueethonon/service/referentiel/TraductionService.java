package org.mosqueethonon.service.referentiel;

import org.mosqueethonon.v1.dto.referentiel.TraductionDto;

public interface TraductionService {

    TraductionDto findTraductionByCleAndValeur(String cle, String valeur);

}
