package org.mosqueethonon.v1.controller;

import org.mosqueethonon.concurrent.LockManager;
import org.mosqueethonon.service.InscriptionEnfantService;
import org.mosqueethonon.service.InscriptionEnfantLightService;
import org.mosqueethonon.v1.criterias.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.InscriptionEnfantLightDto;
import org.mosqueethonon.v1.dto.InscriptionSaveCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

@RestController
@RequestMapping(path = "/v1/inscriptions-enfants")
public class InscriptionEnfantController {

    @Autowired
    private InscriptionEnfantService inscriptionEnfantService;
    @Autowired
    private InscriptionEnfantLightService inscriptionEnfantLightService;

    @Autowired
    private LockManager lockManager;

    @PostMapping
    public ResponseEntity<InscriptionEnfantDto> saveInscription(@RequestBody InscriptionEnfantDto inscription,
                                                                @ModelAttribute InscriptionSaveCriteria criteria) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            inscription = this.inscriptionEnfantService.saveInscription(inscription, criteria);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.ok(inscription);
    }

    @GetMapping
    public ResponseEntity<List<InscriptionEnfantLightDto>> findInscriptionsByCriteria(@ModelAttribute InscriptionCriteria criteria) {
        List<InscriptionEnfantLightDto> personnes = this.inscriptionEnfantLightService.findInscriptionsEnfantLightByCriteria(criteria);
        return ResponseEntity.ok(personnes);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<InscriptionEnfantDto> findInscriptionById(@PathVariable("id") Long id) {
        InscriptionEnfantDto personne = this.inscriptionEnfantService.findInscriptionById(id);
        return ResponseEntity.ok(personne);
    }

    @PostMapping(path = "/validation")
    public ResponseEntity validateInscriptions(@RequestBody Set<Long> ids) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            ids = this.inscriptionEnfantService.validateInscriptions(ids);
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
            ids = this.inscriptionEnfantService.deleteInscriptions(ids);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.ok(ids);
    }

    @PostMapping(path = "/incoherences")
    public ResponseEntity<String> checkCoherence(@RequestBody InscriptionEnfantDto inscriptionEnfantDto) {
        String incoherence = this.inscriptionEnfantService.checkCoherence(inscriptionEnfantDto);
        return ResponseEntity.ok(incoherence);
    }

}
