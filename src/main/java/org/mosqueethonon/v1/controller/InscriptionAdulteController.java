package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.inscription.InscriptionAdulteService;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteResultDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.inscription.ReinscriptionAdulteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1/inscriptions-adultes")
public class InscriptionAdulteController {

    @Autowired
    private InscriptionAdulteService inscriptionAdulteService;

    @PostMapping
    public ResponseEntity<InscriptionAdulteResultDto> createInscription(@RequestBody InscriptionAdulteDto inscription) {
        InscriptionAdulteResultDto response = this.inscriptionAdulteService.createInscription(inscription);
        return ResponseEntity.ok(response);
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

    @PostMapping(path = "/reinscription")
    public ResponseEntity<InscriptionAdulteDto> reinscription(@RequestBody ReinscriptionAdulteDto reinscriptionAdulteDto) {
        InscriptionAdulteDto inscription = this.inscriptionAdulteService.reinscription(reinscriptionAdulteDto);
        return ResponseEntity.ok(inscription);
    }

}
