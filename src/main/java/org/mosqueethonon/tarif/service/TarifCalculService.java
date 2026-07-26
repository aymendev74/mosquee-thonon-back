package org.mosqueethonon.tarif.service;

import org.mosqueethonon.enums.StatutProfessionnelEnum;
import org.mosqueethonon.tarif.enums.TypeTarifEnum;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantInfosDto;
import org.mosqueethonon.tarif.v1.dto.TarifInscriptionAdulteDto;
import org.mosqueethonon.tarif.v1.dto.TarifInscriptionEnfantDto;

import java.time.LocalDate;

public interface TarifCalculService {

    public TarifInscriptionEnfantDto calculTarifInscriptionEnfant(Long id, InscriptionEnfantInfosDto inscriptionInfos);

    public TarifInscriptionAdulteDto calculTarifInscriptionAdulte(Long id, LocalDate atDate, StatutProfessionnelEnum statutPro);

}
