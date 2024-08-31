package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.InscriptionEnfantInfosDto;
import org.mosqueethonon.v1.dto.TarifInscriptionAdulteDto;
import org.mosqueethonon.v1.dto.TarifInscriptionEnfantDto;

import java.time.LocalDate;

public interface TarifCalculService {

    public TarifInscriptionEnfantDto calculTarifInscriptionEnfant(InscriptionEnfantInfosDto inscriptionInfos);

    public TarifInscriptionAdulteDto calculTarifInscriptionAdulte(LocalDate atDate);

}
