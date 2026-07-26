package org.mosqueethonon.referentiel.v1.controller;

import org.mosqueethonon.lock.concurrent.LockManager;
import org.mosqueethonon.referentiel.service.PeriodeService;
import org.mosqueethonon.referentiel.v1.dto.PeriodeDto;
import org.mosqueethonon.referentiel.v1.dto.PeriodeInfoDto;
import org.mosqueethonon.referentiel.v1.dto.PeriodeValidationResultDto;
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
        periode = this.periodeService.createPeriode(periode);
        return ResponseEntity.ok(periode);
    }


    @PutMapping(path = "/{id}")
    public ResponseEntity<PeriodeDto> updatePeriode(@PathVariable("id") Long id, @RequestBody PeriodeDto periode) {
        periode = this.periodeService.updatePeriode(id, periode);
        return ResponseEntity.ok(periode);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deletePeriode(@PathVariable("id") Long id) {
        this.periodeService.deletePeriode(id);
        return ResponseEntity.ok().build();
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
