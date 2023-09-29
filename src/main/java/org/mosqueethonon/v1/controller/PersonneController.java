package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.PersonneService;
import org.mosqueethonon.service.criteria.PersonneCriteria;
import org.mosqueethonon.v1.dto.PersonneDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "v1/inscriptions/")
@CrossOrigin
public class PersonneController {

    @Autowired
    private PersonneService personneService;

    @PostMapping
    public ResponseEntity<PersonneDto> saveInscription(@RequestBody PersonneDto personne) {
        personne = this.personneService.savePersonne(personne);
        return ResponseEntity.ok(personne);
    }

    @GetMapping
    public ResponseEntity<List<PersonneDto>> findInscriptionsByCriteria(@ModelAttribute PersonneCriteria criteria) {
        List<PersonneDto> personnes = this.personneService.findPersonneByCriteria(criteria);
        return ResponseEntity.ok(personnes);
    }

    @GetMapping(path = "{id}")
    public ResponseEntity<PersonneDto> findInscriptionById(@PathVariable("id") Long id) {
        PersonneDto personne = this.personneService.findInscriptionById(id);
        return ResponseEntity.ok(personne);
    }
}
