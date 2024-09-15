package org.mosqueethonon.v1.controller;

import org.mosqueethonon.concurrent.LockManager;
import org.mosqueethonon.service.referentiel.PeriodeService;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;
import org.mosqueethonon.v1.dto.referentiel.PeriodeInfoDto;
import org.mosqueethonon.v1.dto.referentiel.PeriodeValidationResultDto;
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
    public ResponseEntity<List<PeriodeInfoDto>> findPeriodesByApplication(@RequestParam(name = "application") String application) {
        List<PeriodeInfoDto> periodes = this.periodeService.findPeriodesByApplication(application);
        return ResponseEntity.ok(periodes);
    }

    @PostMapping
    public ResponseEntity<PeriodeDto> createPeriode(@RequestBody PeriodeDto periode) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            periode = this.periodeService.createPeriode(periode);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.ok(periode);
    }


    @PutMapping(path = "/{id}")
    public ResponseEntity<PeriodeDto> updatePeriode(@PathVariable("id") Long id, @RequestBody PeriodeDto periode) {
        Lock lock = lockManager.getLock(LockManager.LOCK_INSCRIPTIONS);
        lock.lock();
        try {
            periode = this.periodeService.updatePeriode(id, periode);
        } finally {
            lock.unlock();
        }
        return ResponseEntity.ok(periode);
    }

    @PostMapping(path = "/validation")
    public ResponseEntity<PeriodeValidationResultDto> validatePeriode(@RequestBody PeriodeDto periode) {
        PeriodeValidationResultDto result = this.periodeService.validatePeriode(null, periode);
        return ResponseEntity.ok(result);
    }

    @PutMapping(path = "/{id}/validation")
    public ResponseEntity<PeriodeValidationResultDto> validateExistingPeriode(@PathVariable("id") Long id, @RequestBody PeriodeDto periode) {
        PeriodeValidationResultDto result = this.periodeService.validatePeriode(id, periode);
        return ResponseEntity.ok(result);
    }
}
