package org.mosqueethonon.service.impl.referentiel;

import lombok.AllArgsConstructor;
import org.mosqueethonon.enums.ApplicationTarifEnum;
import org.mosqueethonon.enums.TypeInscriptionEnum;
import org.mosqueethonon.enums.TypeTarifEnum;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.service.param.ParamService;
import org.mosqueethonon.service.referentiel.TarifCalculService;
import org.mosqueethonon.service.referentiel.TarifService;
import org.mosqueethonon.service.criteria.TarifCriteria;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantInfosDto;
import org.mosqueethonon.v1.dto.referentiel.PeriodeInfoDto;
import org.mosqueethonon.v1.dto.referentiel.TarifDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionAdulteDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionEnfantDto;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class TarifCalculServiceImpl implements TarifCalculService {

    private TarifService tarifService;
    private InscriptionRepository inscriptionRepository;

    private ParamService paramService;

    private SecurityContext securityContext;

    @Override
    public TarifInscriptionEnfantDto calculTarifInscriptionEnfant(InscriptionEnfantInfosDto inscriptionInfos) {
        // Uniquement hors mode admin, si les inscriptions sont désactivées, on ne va pas plus loin
        if(!this.securityContext.isAdmin()) {
            boolean isInscriptionEnabled = this.paramService.isInscriptionEnabled();
            if(!isInscriptionEnabled) {
                return null;
            }
        }

        Boolean adherent = inscriptionInfos.getAdherent();
        Integer nbEnfants = inscriptionInfos.getNbEleves();
        LocalDate atDate = inscriptionInfos.getAtDate() != null ? inscriptionInfos.getAtDate() : LocalDate.now();

        // Calcul du tarif de base
        TarifCriteria criteria = TarifCriteria.builder().application(ApplicationTarifEnum.COURS_ENFANT.name())
                .type(TypeTarifEnum.BASE.name()).adherent(adherent)
                .nbEnfant(nbEnfants).atDate(atDate).build();
        List<TarifDto> tarifsBase = this.tarifService.findTarifByCriteria(criteria);
        if(CollectionUtils.isEmpty(tarifsBase)) {
            // Si pas de tarif base trouvé, alors on ne peut pas donner de tarif à l'utilisateur
            return null;
        }

        // Calcul du tarif par enfant
        criteria = TarifCriteria.builder().application(ApplicationTarifEnum.COURS_ENFANT.name())
                .type(TypeTarifEnum.ENFANT.name()).adherent(adherent)
                .nbEnfant(nbEnfants).atDate(atDate).build();
        List<TarifDto> tarifsEnfant = this.tarifService.findTarifByCriteria(criteria);
        if(CollectionUtils.isEmpty(tarifsEnfant)) {
            // Si pas de tarif enfant trouvé, alors on ne peut pas donner de tarif à l'utilisateur
            return null;
        }

        // On va aller calculer si le nombre d'élèves maximum sur la période risque d'être atteint
        // afin d'avertir l'utilisateur
        TarifDto tarifBase = tarifsBase.get(0);
        TarifDto tarifEnfant = tarifsEnfant.get(0);
        PeriodeInfoDto periode = tarifEnfant.getPeriode();
        Integer nbElevesInscrits = this.inscriptionRepository.getNbElevesInscritsByIdPeriode(periode.getId(), TypeInscriptionEnum.ENFANT.name());
        boolean isListeAttente = nbEnfants + nbElevesInscrits > periode.getNbMaxInscription();

        return TarifInscriptionEnfantDto.builder().tarifBase(tarifBase.getMontant()).idTariBase(tarifBase.getId())
                .tarifEleve(tarifEnfant.getMontant()).idTariEleve(tarifEnfant.getId())
                .listeAttente(isListeAttente).build();
    }

    @Override
    public TarifInscriptionAdulteDto calculTarifInscriptionAdulte(LocalDate atDate) {
        TarifCriteria criteria = TarifCriteria.builder().application(ApplicationTarifEnum.COURS_ADULTE.name())
                .type(TypeTarifEnum.ADULTE.name()).atDate(atDate).build();
        List<TarifDto> tarifsBase = this.tarifService.findTarifByCriteria(criteria);
        if(!CollectionUtils.isEmpty(tarifsBase)) {
            TarifDto tarif = tarifsBase.get(0);
            return TarifInscriptionAdulteDto.builder().idTari(tarif.getId()).tarif(tarif.getMontant()).build();
        }
        return null;
    }
}