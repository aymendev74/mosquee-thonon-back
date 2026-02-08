package org.mosqueethonon.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.concurrent.LockManager;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.service.inscription.InscriptionOrchestratorService;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionParAnneeScolaireDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.inscription.ReinscriptionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.locks.Lock;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/v1/inscriptions-enfants")
public class InscriptionEnfantController {

    private InscriptionEnfantService inscriptionEnfantService;

    private InscriptionOrchestratorService inscriptionOrchestratorService;

    private LockManager lockManager;

    @PostMapping
    public ResponseEntity<InscriptionEnfantDto> createInscription(@RequestBody InscriptionEnfantDto inscription) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            inscription = this.inscriptionEnfantService.createInscription(inscription);
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
            inscription = this.inscriptionOrchestratorService.updateInscription(id, inscription, criteria);
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

    @GetMapping(path = "/mes-inscriptions")
    public ResponseEntity<List<InscriptionParAnneeScolaireDto>> getMesInscriptions() {
        List<InscriptionParAnneeScolaireDto> inscriptions = this.inscriptionEnfantService.findInscriptionsByUtilisateurConnecte();
        return ResponseEntity.ok(inscriptions);
    }

    @PostMapping(path = "/reinscription")
    public ResponseEntity<InscriptionEnfantDto> reinscription(@RequestBody ReinscriptionDto reinscriptionDto) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            InscriptionEnfantDto inscription = this.inscriptionEnfantService.reinscription(reinscriptionDto);
            return ResponseEntity.ok(inscription);
        } finally {
            lock.unlock();
        }
    }

}
