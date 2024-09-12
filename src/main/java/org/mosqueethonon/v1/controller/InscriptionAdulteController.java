package org.mosqueethonon.v1.controller;

import org.mosqueethonon.concurrent.LockManager;
import org.mosqueethonon.service.InscriptionAdulteService;
import org.mosqueethonon.v1.dto.InscriptionAdulteDto;
import org.mosqueethonon.v1.dto.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.InscriptionSaveCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.locks.Lock;

@RestController
@RequestMapping(path = "/v1/inscriptions-adultes")
public class InscriptionAdulteController {

    @Autowired
    private InscriptionAdulteService inscriptionAdulteService;

    @PostMapping
    public ResponseEntity<InscriptionAdulteDto> createInscription(@RequestBody InscriptionAdulteDto inscription,
                                                                  @ModelAttribute InscriptionSaveCriteria criteria) {
        inscription = this.inscriptionAdulteService.createInscription(inscription, criteria);
        return ResponseEntity.ok(inscription);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<InscriptionAdulteDto> updateInscription(@PathVariable("id") Long id,
                                                                  @RequestBody InscriptionAdulteDto inscription,
                                                                  @ModelAttribute InscriptionSaveCriteria criteria) {
        inscription = this.inscriptionAdulteService.updateInscription(id, inscription, criteria);
        return ResponseEntity.ok(inscription);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<InscriptionAdulteDto> findInscriptionById(@PathVariable("id") Long id) {
        InscriptionAdulteDto inscription = this.inscriptionAdulteService.findInscriptionById(id);
        return ResponseEntity.ok(inscription);
    }

}
