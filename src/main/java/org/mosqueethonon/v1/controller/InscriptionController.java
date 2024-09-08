package org.mosqueethonon.v1.controller;

import org.mosqueethonon.concurrent.LockManager;
import org.mosqueethonon.service.InscriptionLightService;
import org.mosqueethonon.service.InscriptionService;
import org.mosqueethonon.v1.criterias.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionLightDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

@RestController
@RequestMapping(path = "/v1/inscriptions")
public class InscriptionController {

    @Autowired
    private InscriptionLightService inscriptionLightService;

    @Autowired
    private InscriptionService inscriptionService;

    @Autowired
    private LockManager lockManager;

    @GetMapping
    public ResponseEntity<List<InscriptionLightDto>> findInscriptionsLightsByCriteria(@ModelAttribute InscriptionCriteria criteria) {
        List<InscriptionLightDto> inscriptionLights = this.inscriptionLightService.findInscriptionsEnfantLightByCriteria(criteria);
        return ResponseEntity.ok(inscriptionLights);
    }


    @PostMapping(path = "/validation")
    public ResponseEntity validateInscriptions(@RequestBody Set<Long> ids) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            ids = this.inscriptionService.validateInscriptions(ids);
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
            ids = this.inscriptionService.deleteInscriptions(ids);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.ok(ids);
    }
}
