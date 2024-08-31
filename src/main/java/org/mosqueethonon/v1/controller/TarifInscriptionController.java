package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.TarifCalculService;
import org.mosqueethonon.v1.dto.InscriptionEnfantInfosDto;
import org.mosqueethonon.v1.dto.TarifInscriptionEnfantDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1/tarifs-inscription")
public class TarifInscriptionController {

    @Autowired
    private TarifCalculService tarifCalculService;

    @PostMapping
    public ResponseEntity<TarifInscriptionEnfantDto> calculTarifInscription(@RequestBody InscriptionEnfantInfosDto inscriptionInfos) {
        TarifInscriptionEnfantDto tarif = this.tarifCalculService.calculTarifInscriptionEnfant(inscriptionInfos);
        if(tarif == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tarif);
    }

}
