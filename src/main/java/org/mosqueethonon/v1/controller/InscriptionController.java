package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.InscriptionService;
import org.mosqueethonon.service.criteria.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "v1/inscriptions/")
@CrossOrigin
public class InscriptionController {

    @Autowired
    private InscriptionService inscriptionService;

    @PostMapping
    public ResponseEntity<InscriptionDto> saveInscription(@RequestBody InscriptionDto personne) {
        personne = this.inscriptionService.savePersonne(personne);
        return ResponseEntity.ok(personne);
    }

    @GetMapping
    public ResponseEntity<List<InscriptionDto>> findInscriptionsByCriteria(@ModelAttribute InscriptionCriteria criteria) {
        List<InscriptionDto> personnes = this.inscriptionService.findPersonneByCriteria(criteria);
        return ResponseEntity.ok(personnes);
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<InscriptionDto> findInscriptionById(@PathVariable("id") Long id) {
        InscriptionDto personne = this.inscriptionService.findInscriptionById(id);
        return ResponseEntity.ok(personne);
    }

    @PostMapping(path = "validation")
    public ResponseEntity validateInscriptions(@RequestBody List<Long> ids) {
        ids = this.inscriptionService.validateInscriptions(ids);
        return ResponseEntity.ok(ids);
    }
}
