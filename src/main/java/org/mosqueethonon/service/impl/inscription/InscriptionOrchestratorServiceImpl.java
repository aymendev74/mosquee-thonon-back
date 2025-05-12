package org.mosqueethonon.service.impl.inscription;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.inscription.InscriptionEntity;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.enums.TypeInscriptionEnum;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.service.inscription.InscriptionOrchestratorService;
import org.mosqueethonon.service.inscription.InscriptionService;
import org.mosqueethonon.service.referentiel.PeriodeService;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service qui permet de casser la dépendance cyclique entre InscriptionEnfantService et PeriodeServide
 * Car la mise à jour d'une des ressources implique dans certains cas une mise à jour de la ressource liée
 * Periode <-----> Inscription
 */

@Service
@AllArgsConstructor
public class InscriptionOrchestratorServiceImpl implements InscriptionOrchestratorService {

    private InscriptionEnfantService inscriptionEnfantService;
    private InscriptionService inscriptionService;
    private PeriodeService periodeService;

    @Override
    @Transactional
    public InscriptionEnfantDto updateInscription(Long id, InscriptionEnfantDto inscription, InscriptionSaveCriteria criteria) {
        InscriptionEnfantDto inscriptionEnfant = this.inscriptionEnfantService.updateInscription(id, inscription, criteria);
        Long idPeriode = this.inscriptionService.getIdPeriodeByIdInscription(inscriptionEnfant.getId());
        this.periodeService.updateNbMaxElevesIfNeeded(idPeriode);

        return inscriptionEnfant;
    }

    @Override
    @Transactional
    public Set<Long> deleteInscriptions(Set<Long> ids) {
        // Mise à jour de la liste d'attente éventuelle après le delete, il nous faut les periodes concernées
        Set<Long> idPeriodes = new HashSet<>();
        for(Long id : ids) {
            InscriptionEntity inscription = this.inscriptionService.findInscriptionById(id);
            if(inscription != null && TypeInscriptionEnum.ENFANT.name().equals(inscription.getType())) {
                idPeriodes.add(this.inscriptionService.getIdPeriodeByIdInscription(id));
            }
        }

        // Delete des inscriptions
        this.inscriptionService.deleteInscriptions(ids);

        // Mise à jour de la liste d'attente
        if(!CollectionUtils.isEmpty(idPeriodes)) {
            for(Long idPeriode : idPeriodes) {
                PeriodeEntity periode = this.periodeService.findPeriodeById(idPeriode);
                this.inscriptionEnfantService.updateListeAttente(periode.getId(), periode.getNbMaxInscription());
            }
        }
        return ids;
    }
}
