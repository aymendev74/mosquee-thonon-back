package org.mosqueethonon.service.referentiel;

import org.mosqueethonon.enums.StatutProfessionnelEnum;
import org.mosqueethonon.enums.TypeTarifEnum;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantInfosDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionAdulteDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionEnfantDto;

import java.time.LocalDate;

public interface TarifCalculService {

    public TarifInscriptionEnfantDto calculTarifInscriptionEnfant(Long id, InscriptionEnfantInfosDto inscriptionInfos);

    public TarifInscriptionAdulteDto calculTarifInscriptionAdulte(Long id, LocalDate atDate, StatutProfessionnelEnum statutPro);

}
