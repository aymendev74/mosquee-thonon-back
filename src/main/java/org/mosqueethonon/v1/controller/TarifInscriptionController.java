package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.referentiel.TarifCalculService;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantInfosDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionAdulteDto;
import org.mosqueethonon.v1.dto.referentiel.TarifInscriptionEnfantDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(path = "/v1/tarifs-inscription")
public class TarifInscriptionController {

    @Autowired
    private TarifCalculService tarifCalculService;

    @GetMapping(path = "/enfant")
    public ResponseEntity<TarifInscriptionEnfantDto> calculTarifInscriptionEnfant(@ModelAttribute InscriptionEnfantInfosDto inscriptionInfos) {
        TarifInscriptionEnfantDto tarif = this.tarifCalculService.calculTarifInscriptionEnfant(null, inscriptionInfos);
        if(tarif == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tarif);
    }

    @GetMapping(path = "/enfant/{id}")
    public ResponseEntity<TarifInscriptionEnfantDto> calculTarifInscriptionEnfant(@PathVariable("id") Long id, @ModelAttribute InscriptionEnfantInfosDto inscriptionInfos) {
        TarifInscriptionEnfantDto tarif = this.tarifCalculService.calculTarifInscriptionEnfant(id, inscriptionInfos);
        if(tarif == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tarif);
    }

    @GetMapping(path = "/adulte")
    public ResponseEntity<TarifInscriptionAdulteDto> calculTarifInscriptionAdulte() {
        TarifInscriptionAdulteDto tarif = this.tarifCalculService.calculTarifInscriptionAdulte(null, LocalDate.now());
        if(tarif == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tarif);
    }

    @GetMapping(path = "/adulte/{id}")
    public ResponseEntity<TarifInscriptionAdulteDto> calculTarifInscriptionAdulte(@PathVariable ("id") Long id) {
        TarifInscriptionAdulteDto tarif = this.tarifCalculService.calculTarifInscriptionAdulte(id, null);
        if(tarif == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tarif);
    }

}
