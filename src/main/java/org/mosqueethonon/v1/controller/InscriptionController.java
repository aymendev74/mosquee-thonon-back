package org.mosqueethonon.v1.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.mosqueethonon.concurrent.LockManager;
import org.mosqueethonon.service.inscription.InscriptionLightService;
import org.mosqueethonon.service.inscription.InscriptionOrchestratorService;
import org.mosqueethonon.service.inscription.InscriptionService;
import org.mosqueethonon.v1.criterias.InscriptionCriteria;
import org.mosqueethonon.v1.dto.inscription.InscriptionLightDto;
import org.springframework.beans.factory.annotation.Autowired;
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
}
