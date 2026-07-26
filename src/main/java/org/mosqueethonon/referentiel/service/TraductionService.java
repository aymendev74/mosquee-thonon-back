package org.mosqueethonon.referentiel.service;

import org.mosqueethonon.referentiel.v1.dto.TraductionDto;

public interface TraductionService {

    TraductionDto findTraductionByCleAndValeur(String cle, String valeur);

}
