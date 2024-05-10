package org.mosqueethonon.v1.controller;

import org.mosqueethonon.concurrent.LockManager;
import org.mosqueethonon.service.PeriodeService;
import org.mosqueethonon.v1.dto.PeriodeDto;
import org.mosqueethonon.v1.dto.PeriodeInfoDto;
import org.mosqueethonon.v1.dto.PeriodeValidationResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.locks.Lock;

@RestController
@RequestMapping(path = "/v1/periodes")
public class PeriodeController {

    @Autowired
    private PeriodeService periodeService;
    @Autowired
    private LockManager lockManager;

    @GetMapping
    public ResponseEntity<List<PeriodeInfoDto>> findPeriodes() {
        List<PeriodeInfoDto> periodes = this.periodeService.findAllPeriodes();
        return ResponseEntity.ok(periodes);
    }

    @PostMapping
    public ResponseEntity<PeriodeDto> savePeriode(@RequestBody PeriodeDto periode) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            periode = this.periodeService.savePeriode(periode);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.ok(periode);
    }

    @PostMapping(path = "/validation")
    public ResponseEntity<PeriodeValidationResultDto> validatePeriode(@RequestBody PeriodeDto periode) {
        PeriodeValidationResultDto result = this.periodeService.validatePeriode(periode);
        return ResponseEntity.ok(result);
    }
}
