package org.mosqueethonon.service.referentiel;

import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantInfosDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionAdulteDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionEnfantDto;

import java.time.LocalDate;

public interface TarifCalculService {

    public TarifInscriptionEnfantDto calculTarifInscriptionEnfant(InscriptionEnfantInfosDto inscriptionInfos);

    public TarifInscriptionAdulteDto calculTarifInscriptionAdulte(LocalDate atDate);

}