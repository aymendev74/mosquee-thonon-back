package org.mosqueethonon.v1.controller;

import org.mosqueethonon.concurrent.LockManager;
import org.mosqueethonon.service.InscriptionEnfantService;
import org.mosqueethonon.service.InscriptionLightService;
import org.mosqueethonon.v1.criterias.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.InscriptionLightDto;
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
    private LockManager lockManager;

    @PostMapping
    public ResponseEntity<InscriptionEnfantDto> createInscription(@RequestBody InscriptionEnfantDto inscription,
                                                                  @ModelAttribute InscriptionSaveCriteria criteria) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            inscription = this.inscriptionEnfantService.createInscription(inscription, criteria);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.ok(inscription);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<InscriptionEnfantDto> updateInscription(@PathVariable("id") Long id, @RequestBody InscriptionEnfantDto inscription,
                                                                  @ModelAttribute InscriptionSaveCriteria criteria) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            inscription = this.inscriptionEnfantService.updateInscription(id, inscription, criteria);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.ok(inscription);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<InscriptionEnfantDto> findInscriptionById(@PathVariable("id") Long id) {
        InscriptionEnfantDto inscription = this.inscriptionEnfantService.findInscriptionById(id);
        return ResponseEntity.ok(inscription);
    }

    @PostMapping(path = "/incoherences")
    public ResponseEntity<String> checkCoherence(@RequestBody InscriptionEnfantDto inscriptionEnfantDto) {
        String incoherence = this.inscriptionEnfantService.checkCoherence(null, inscriptionEnfantDto);
        return ResponseEntity.ok(incoherence);
    }

    @PostMapping(path = "/{id}/incoherences")
    public ResponseEntity<String> checkCoherenceInscription(@PathVariable("id") Long idInscription, @RequestBody InscriptionEnfantDto inscriptionEnfantDto) {
        String incoherence = this.inscriptionEnfantService.checkCoherence(idInscription, inscriptionEnfantDto);
        return ResponseEntity.ok(incoherence);
    }

}
