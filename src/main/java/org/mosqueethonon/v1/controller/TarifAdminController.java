package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.TarifAdminService;
import org.mosqueethonon.v1.dto.InfoTarifDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/tarifs-admin")
@CrossOrigin
public class TarifAdminController {

    @Autowired
    private TarifAdminService tarifAdminService;

    @GetMapping("/{idPeriode}")
    public ResponseEntity<InfoTarifDto> findTarifsByPeriode(@PathVariable("idPeriode") Long idPeriode) {
        InfoTarifDto infoTarif = this.tarifAdminService.findInfoTarifByPeriode(idPeriode);
        return ResponseEntity.ok(infoTarif);
    }

    @PostMapping
    public ResponseEntity<InfoTarifDto> saveInfoTarif(@RequestBody InfoTarifDto infoTarifDto) {
        infoTarifDto = this.tarifAdminService.saveInfoTarif(infoTarifDto);
        return ResponseEntity.ok(infoTarifDto);
    }

}
