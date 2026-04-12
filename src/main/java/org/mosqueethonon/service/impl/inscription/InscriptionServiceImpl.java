package org.mosqueethonon.service.impl.inscription;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import org.mosqueethonon.configuration.security.context.SecurityContext;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionEntity;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.enums.MailRequestType;
import org.mosqueethonon.enums.ResourceTypeEnum;
import org.mosqueethonon.exception.BadRequestException;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.BulletinRepository;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.repository.EleveFeuillePresenceRepository;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.LienClasseEleveRepository;
import org.mosqueethonon.repository.MailRequestRepository;
import org.mosqueethonon.service.inscription.InscriptionService;
import org.mosqueethonon.service.lock.LockService;
import org.mosqueethonon.service.referentiel.PeriodeService;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InscriptionServiceImpl implements InscriptionService {

    private MailRequestRepository mailRequestRepository;
    private InscriptionRepository inscriptionRepository;
    private PeriodeService periodeService;
    private LockService lockService;
    private SecurityContext securityContext;
    private LienClasseEleveRepository lienClasseEleveRepository;
    private EleveFeuillePresenceRepository eleveFeuillePresenceRepository;
    private BulletinRepository bulletinRepository;
    private DocumentRequestRepository documentRequestRepository;

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
        this.lockService.acquireLock(ResourceTypeEnum.INSCRIPTION, id, this.securityContext.getUser());
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
        this.lockService.releaseLock(ResourceTypeEnum.INSCRIPTION, id, this.securityContext.getUser());
        return id;
    }

    @Transactional
    @Override
    public Set<Long> deleteInscriptions(Set<Long> ids) {
        for(Long id : ids) {
            this.lockService.acquireLock(ResourceTypeEnum.INSCRIPTION, id, this.securityContext.getUser());
            InscriptionEntity inscription = this.inscriptionRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Inscription with id " + id + " does not exist !"));
            List<Long> eleveIds = inscription.getEleves().stream()
                    .map(EleveEntity::getId)
                    .collect(Collectors.toList());
            if (!eleveIds.isEmpty()) {
                List<BulletinEntity> bulletins = this.bulletinRepository.findByIdEleveIn(eleveIds);
                if (!bulletins.isEmpty()) {
                    Set<Long> bulletinIds = bulletins.stream()
                            .map(BulletinEntity::getId)
                            .collect(Collectors.toSet());
                    this.documentRequestRepository.deleteByTypeAndBusinessIdIn(DocumentRequestType.BULLETIN, bulletinIds);
                    this.bulletinRepository.deleteAll(bulletins);
                }
                this.eleveFeuillePresenceRepository.deleteByEleveIdIn(eleveIds);
                this.lienClasseEleveRepository.deleteByEleveIdIn(eleveIds);
            }
            this.mailRequestRepository.deleteByTypeAndBusinessIdIn(MailRequestType.INSCRIPTION, Sets.newHashSet(id));
            this.inscriptionRepository.deleteById(id);
            this.lockService.releaseLock(ResourceTypeEnum.INSCRIPTION, id, this.securityContext.getUser());
        }
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
