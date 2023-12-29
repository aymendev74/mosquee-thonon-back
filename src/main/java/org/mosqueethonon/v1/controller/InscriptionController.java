package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.InscriptionLightService;
import org.mosqueethonon.service.InscriptionService;
import org.mosqueethonon.service.criteria.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionDto;
import org.mosqueethonon.v1.dto.InscriptionLightDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/inscriptions")
@CrossOrigin
public class InscriptionController {

    @Autowired
    private InscriptionService inscriptionService;
    @Autowired
    private InscriptionLightService inscriptionLightService;

    @PostMapping
    public ResponseEntity<InscriptionDto> saveInscription(@RequestBody InscriptionDto inscription) {
        inscription = this.inscriptionService.savePersonne(inscription);
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
    public ResponseEntity validateInscriptions(@RequestBody List<Long> ids) {
        ids = this.inscriptionService.validateInscriptions(ids);
        return ResponseEntity.ok(ids);
    }

    @DeleteMapping
    public ResponseEntity deleteInscriptions(@RequestBody List<Long> ids) {
        ids = this.inscriptionService.deleteInscriptions(ids);
        return ResponseEntity.ok(ids);
    }
}
