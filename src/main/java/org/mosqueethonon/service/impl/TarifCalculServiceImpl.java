package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.enums.ApplicationTarifEnum;
import org.mosqueethonon.enums.TypeTarifEnum;
import org.mosqueethonon.service.TarifCalculService;
import org.mosqueethonon.service.TarifService;
import org.mosqueethonon.service.criteria.TarifCriteria;
import org.mosqueethonon.v1.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TarifCalculServiceImpl implements TarifCalculService {

    private TarifService tarifService;

    @Override
    public TarifInscriptionDto calculTarifInscription(InscriptionInfosDto inscriptionInfos) {
        ResponsableLegalDto responsableLegal = inscriptionInfos.getResponsableLegal();
        List<EleveDto> eleves = inscriptionInfos.getEleves();
        Integer nbEnfants = eleves.size();

        // Calcul du tarif de base
        TarifCriteria criteria = TarifCriteria.builder().application(ApplicationTarifEnum.COURS.name())
                .type(TypeTarifEnum.BASE.name()).adherent(responsableLegal.getAdherent())
                .nbEnfant(nbEnfants).build();
        List<TarifDto> tarifsBase = this.tarifService.findTarifByCriteria(criteria);
        TarifDto tarifBase = tarifsBase.get(0);

        // Calcul du tarif par enfant
        criteria = TarifCriteria.builder().application(ApplicationTarifEnum.COURS.name())
                .type(TypeTarifEnum.ENFANT.name()).adherent(responsableLegal.getAdherent())
                .nbEnfant(nbEnfants).build();
        List<TarifDto> tarifsEnfant = this.tarifService.findTarifByCriteria(criteria);
        TarifDto tarifEnfant = tarifsEnfant.get(0);

        return TarifInscriptionDto.builder().tarifBase(tarifBase.getMontant()).idTariBase(tarifBase.getId())
                .tarifEleve(tarifEnfant.getMontant()).idTariEleve(tarifEnfant.getId()).build();
    }

}
