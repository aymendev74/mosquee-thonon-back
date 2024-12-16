package org.mosqueethonon.service.impl.inscription;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.inscription.InscriptionEntity;
import org.mosqueethonon.exception.BadRequestException;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.service.inscription.InscriptionService;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@AllArgsConstructor
public class InscriptionServiceImpl implements InscriptionService {

    private InscriptionRepository inscriptionRepository;

    private InscriptionEnfantService inscriptionEnfantService;

    @Transactional
    @Override
    public Set<Long> patchInscriptions(JsonNode patchesNode) {
        Set<Long> ids = new HashSet<>();
        if(patchesNode.has("inscriptions") && patchesNode.get("inscriptions").elements().hasNext()) {
            patchesNode.get("inscriptions").forEach(node -> ids.add(this.patchInscription(node)));
        }
        return ids;
    }

    private Long patchInscription(JsonNode patchNode) {
        if (!patchNode.has("id") || !patchNode.get("id").isNumber()) {
            throw new BadRequestException("Missing 'id' field or wrong type (expect Long) to patch inscription !");
        }
        Long id = patchNode.get("id").asLong();
        InscriptionEntity inscription = this.inscriptionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inscription with id " + id + " does not exist !"));
        if (patchNode.has("statut")) {
            if(patchNode.get("statut").isNull()) {
                inscription.setStatut(null);
            } else {
                StatutInscription statut = StatutInscription.valueOf(patchNode.get("statut").asText());
                inscription.setStatut(statut);
                if(statut == StatutInscription.VALIDEE) {
                    inscription.setNoPositionAttente(null);
                }
            }
        }
        this.inscriptionRepository.save(inscription);
        return id;
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
