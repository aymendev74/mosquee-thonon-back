package org.mosqueethonon.v1.controller;

import org.mosqueethonon.concurrent.LockManager;
import org.mosqueethonon.service.TarifAdminService;
import org.mosqueethonon.v1.dto.InfoTarifDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.locks.Lock;

@RestController
@RequestMapping(path = "/v1/tarifs-admin")
public class TarifAdminController {

    @Autowired
    private TarifAdminService tarifAdminService;
    @Autowired
    private LockManager lockManager;

    @GetMapping("/{idPeriode}")
    public ResponseEntity<InfoTarifDto> findTarifsByPeriode(@PathVariable("idPeriode") Long idPeriode) {
        InfoTarifDto infoTarif = this.tarifAdminService.findInfoTarifByPeriode(idPeriode);
        return ResponseEntity.ok(infoTarif);
    }

    @PostMapping
    public ResponseEntity<InfoTarifDto> saveInfoTarif(@RequestBody InfoTarifDto infoTarifDto) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            infoTarifDto = this.tarifAdminService.saveInfoTarif(infoTarifDto);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.ok(infoTarifDto);
    }

}
