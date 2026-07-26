package org.mosqueethonon.inscription.v1.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.mosqueethonon.lock.concurrent.LockManager;
import org.mosqueethonon.inscription.service.InscriptionLightService;
import org.mosqueethonon.inscription.service.InscriptionOrchestratorService;
import org.mosqueethonon.inscription.service.InscriptionService;
import org.mosqueethonon.inscription.service.MesInscriptionsService;
import org.mosqueethonon.inscription.v1.criteria.InscriptionCriteria;
import org.mosqueethonon.inscription.v1.dto.InscriptionLightDto;
import org.mosqueethonon.inscription.v1.dto.MesInscriptionsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/v1/inscriptions")
public class InscriptionController {

    private InscriptionLightService inscriptionLightService;

    private InscriptionService inscriptionService;

    private InscriptionOrchestratorService inscriptionOrchestratorService;

    private MesInscriptionsService mesInscriptionsService;

    private LockManager lockManager;

    @GetMapping
    public ResponseEntity<List<InscriptionLightDto>> findInscriptionsLightsByCriteria(@ModelAttribute InscriptionCriteria criteria) {
        List<InscriptionLightDto> inscriptionLights = this.inscriptionLightService.findInscriptionsEnfantLightByCriteria(criteria);
        return ResponseEntity.ok(inscriptionLights);
    }

    @PatchMapping
    public ResponseEntity patchInscriptions(@RequestBody JsonNode patchesNode) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        Set<Long> ids = null;
        try {
            ids = this.inscriptionService.patchInscriptions(patchesNode);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.ok(ids);
    }

    @DeleteMapping
    public ResponseEntity deleteInscriptions(@RequestBody Set<Long> ids) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            ids = this.inscriptionOrchestratorService.deleteInscriptions(ids);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.ok(ids);
    }

    @GetMapping(path = "/mes-inscriptions")
    public ResponseEntity<MesInscriptionsDto> getMesInscriptions() {
        MesInscriptionsDto inscriptions = this.mesInscriptionsService.findMesInscriptions();
        return ResponseEntity.ok(inscriptions);
    }
}
