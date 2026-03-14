package org.mosqueethonon.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.service.inscription.InscriptionOrchestratorService;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantResultDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.inscription.ReinscriptionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(path = "/v1/inscriptions-enfants")
public class InscriptionEnfantController {

    private InscriptionEnfantService inscriptionEnfantService;

    private InscriptionOrchestratorService inscriptionOrchestratorService;

    @PostMapping
    public ResponseEntity<InscriptionEnfantResultDto> createInscription(@RequestBody InscriptionEnfantDto inscription) {
        InscriptionEnfantResultDto response = this.inscriptionEnfantService.createInscription(inscription);
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<InscriptionEnfantDto> updateInscription(@PathVariable("id") Long id, @RequestBody InscriptionEnfantDto inscription,
                                                                  @ModelAttribute InscriptionSaveCriteria criteria) {
        inscription = this.inscriptionOrchestratorService.updateInscription(id, inscription, criteria);
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

    @PostMapping(path = "/reinscription")
    public ResponseEntity<InscriptionEnfantDto> reinscription(@RequestBody ReinscriptionDto reinscriptionDto) {
        InscriptionEnfantDto inscription = this.inscriptionEnfantService.reinscription(reinscriptionDto);
        return ResponseEntity.ok(inscription);
    }

}
