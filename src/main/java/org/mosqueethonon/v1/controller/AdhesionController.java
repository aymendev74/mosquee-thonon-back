package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.AdhesionLightService;
import org.mosqueethonon.service.AdhesionService;
import org.mosqueethonon.v1.criterias.AdhesionCriteria;
import org.mosqueethonon.v1.dto.AdhesionDto;
import org.mosqueethonon.v1.dto.AdhesionLightDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "api/v1/adhesions")
@CrossOrigin
public class AdhesionController {

    @Autowired
    private AdhesionService adhesionService;
    @Autowired
    private AdhesionLightService adhesionLightService;

    @PostMapping
    public ResponseEntity<AdhesionDto> saveAdhesion(@RequestBody AdhesionDto adhesion) {
        adhesion = this.adhesionService.saveAdhesion(adhesion);
        return ResponseEntity.ok(adhesion);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<AdhesionDto> findAdhesionById(@PathVariable("id") Long id) {
        AdhesionDto adhesion = this.adhesionService.findAdhesionById(id);
        return ResponseEntity.ok(adhesion);
    }

    @DeleteMapping
    public ResponseEntity deleteAdhesions(@RequestBody Set<Long> ids) {
        ids = this.adhesionService.deleteAdhesions(ids);
        return ResponseEntity.ok(ids);
    }

    @PostMapping(path = "/validation")
    public ResponseEntity validateInscriptions(@RequestBody Set<Long> ids) {
        ids = this.adhesionService.validateAdhesions(ids);
        return ResponseEntity.ok(ids);
    }

    @GetMapping
    public ResponseEntity<List<AdhesionLightDto>> findAdhesionsByCriteria(@ModelAttribute AdhesionCriteria criteria) {
        List<AdhesionLightDto> adhesions = this.adhesionLightService.findAdhesionsLightByCriteria(criteria);
        return ResponseEntity.ok(adhesions);
    }
}
