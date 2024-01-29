package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.TarifCalculService;
import org.mosqueethonon.v1.dto.InscriptionInfosDto;
import org.mosqueethonon.v1.dto.TarifInscriptionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(path = "api/v1/tarifs-inscription")
public class TarifInscriptionController {

    @Autowired
    private TarifCalculService tarifCalculService;

    @PostMapping
    public ResponseEntity<TarifInscriptionDto> calculTarifInscription(@RequestBody InscriptionInfosDto inscriptionInfos) {
        TarifInscriptionDto tarif = this.tarifCalculService.calculTarifInscription(inscriptionInfos);
        if(tarif == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tarif);
    }

}
