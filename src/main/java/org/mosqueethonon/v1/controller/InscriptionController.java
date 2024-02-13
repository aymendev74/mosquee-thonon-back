package org.mosqueethonon.v1.controller;

import org.mosqueethonon.concurrent.LockManager;
import org.mosqueethonon.service.InscriptionLightService;
import org.mosqueethonon.service.InscriptionService;
import org.mosqueethonon.service.criteria.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionDto;
import org.mosqueethonon.v1.dto.InscriptionLightDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

@RestController
@RequestMapping(path = "api/v1/inscriptions")
@CrossOrigin
public class InscriptionController {

    @Autowired
    private InscriptionService inscriptionService;
    @Autowired
    private InscriptionLightService inscriptionLightService;

    @Autowired
    private LockManager lockManager;

    @PostMapping
    public ResponseEntity<InscriptionDto> saveInscription(@RequestBody InscriptionDto inscription) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            inscription = this.inscriptionService.saveInscription(inscription);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.ok(inscription);
    }

    @GetMapping
    public ResponseEntity<List<InscriptionLightDto>> findInscriptionsByCriteria(@ModelAttribute InscriptionCriteria criteria) {
        List<InscriptionLightDto> personnes = this.inscriptionLightService.findInscriptionsLightByCriteria(criteria);
        return ResponseEntity.ok(personnes);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<InscriptionDto> findInscriptionById(@PathVariable("id") Long id) {
        InscriptionDto personne = this.inscriptionService.findInscriptionById(id);
        return ResponseEntity.ok(personne);
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
