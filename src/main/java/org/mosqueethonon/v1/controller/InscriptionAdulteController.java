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
    public ResponseEntity<InscriptionAdulteDto> saveInscription(@RequestBody InscriptionAdulteDto inscription,
                                                                @ModelAttribute InscriptionSaveCriteria criteria) {
        inscription = this.inscriptionAdulteService.saveInscription(inscription, criteria);
        return ResponseEntity.ok(inscription);
    }

}