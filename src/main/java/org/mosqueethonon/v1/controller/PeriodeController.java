package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.PeriodeService;
import org.mosqueethonon.v1.dto.PeriodeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "v1/periodes")
@CrossOrigin
public class PeriodeController {

    @Autowired
    private PeriodeService periodeService;

    @GetMapping
    public ResponseEntity<List<PeriodeDto>> findPeriodes() {
        List<PeriodeDto> periodes = this.periodeService.findAllPeriodes();
        return ResponseEntity.ok(periodes);
    }
}
