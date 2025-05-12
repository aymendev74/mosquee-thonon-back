package org.mosqueethonon.service.impl.inscription;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.entity.inscription.InscriptionEntity;
import org.mosqueethonon.exception.BadRequestException;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.service.inscription.InscriptionService;
import org.mosqueethonon.service.referentiel.PeriodeService;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@AllArgsConstructor
public class InscriptionServiceImpl implements InscriptionService {

    private InscriptionRepository inscriptionRepository;
    private PeriodeService periodeService;

    @Transactional
    @Override
    public Set<Long> patchInscriptions(JsonNode patchesNode) {
        Set<Long> ids = new HashSet<>();
        if(patchesNode.has("inscriptions") && patchesNode.get("inscriptions").elements().hasNext()) {
            patchesNode.get("inscriptions").forEach(node -> ids.add(this.patchInscription(node)));
        } else {
            throw new BadRequestException("Missing non empty 'inscriptions' field to patch inscriptions !");
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
        Long idPeriode = this.inscriptionRepository.getIdPeriodeByIdInscription(inscription.getId());
        this.periodeService.updateNbMaxElevesIfNeeded(idPeriode);
        this.inscriptionRepository.save(inscription);
        return id;
    }

    @Transactional
    @Override
    public Set<Long> deleteInscriptions(Set<Long> ids) {
        this.inscriptionRepository.deleteAllById(ids);
        return ids;
    }

    @Override
    public Long getIdPeriodeByIdInscription(Long idInscription) {
        return this.inscriptionRepository.getIdPeriodeByIdInscription(idInscription);
    }

    @Override
    public InscriptionEntity findInscriptionById(Long id) {
        return this.inscriptionRepository.findById(id).orElse(null);
    }
}
