package org.mosqueethonon.service.impl.inscription;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.inscription.InscriptionEntity;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.service.inscription.InscriptionService;
import org.mosqueethonon.v1.dto.inscription.InscriptionPatchDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InscriptionServiceImpl implements InscriptionService {

    private InscriptionRepository inscriptionRepository;

    private InscriptionEnfantService inscriptionEnfantService;

    @Transactional
    @Override
    public Set<Long> patchInscriptions(InscriptionPatchDto inscriptionPatchDto) {
        List<InscriptionEntity> inscriptionsToUpdate = new ArrayList<>();
        for (Long id : inscriptionPatchDto.getIds()) {
            InscriptionEntity inscription = this.inscriptionRepository.findById(id).orElse(null);
            if(inscription!=null) {
                inscription.setStatut(inscriptionPatchDto.getStatut());
                if(inscriptionPatchDto.getStatut().equals(StatutInscription.VALIDEE)) {
                    inscription.setNoPositionAttente(null);
                }
                inscriptionsToUpdate.add(inscription);
            }
        }
        if(!CollectionUtils.isEmpty(inscriptionsToUpdate)) {
            inscriptionsToUpdate = this.inscriptionRepository.saveAll(inscriptionsToUpdate);
            return inscriptionsToUpdate.stream().map(InscriptionEntity::getId).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    @Transactional
    @Override
    public Set<Long> deleteInscriptions(Set<Long> ids) {
        List<InscriptionEntity> inscriptions = this.inscriptionRepository.findAllById(ids);
        boolean isInscriptionEnfant = inscriptions.stream().anyMatch(i -> i.getType().equals("ENFANT"));
        this.inscriptionRepository.deleteAllById(ids);
        // Maintenant que des inscriptions ont été supprimés, il faut aller voir si des inscriptions sont en liste d'attente et
        // les changer de statut => provisoire
        if(isInscriptionEnfant) { // uniquement si inscription enfant, car pas de liste d'attente pour les adultes
            this.inscriptionEnfantService.updateListeAttentePeriode(null);
        }
        return ids;
    }

}
