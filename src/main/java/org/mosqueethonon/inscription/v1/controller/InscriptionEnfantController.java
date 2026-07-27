package org.mosqueethonon.inscription.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.inscription.service.InscriptionEnfantService;
import org.mosqueethonon.inscription.service.InscriptionOrchestratorService;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantResultDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionSaveCriteria;
import org.mosqueethonon.inscription.v1.dto.ReinscriptionDto;
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
