package org.mosqueethonon.v1.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import org.mosqueethonon.service.adhesion.AdhesionLightService;
import org.mosqueethonon.service.adhesion.AdhesionService;
import org.mosqueethonon.v1.criterias.AdhesionCriteria;
import org.mosqueethonon.v1.dto.adhesion.AdhesionDto;
import org.mosqueethonon.v1.dto.adhesion.AdhesionLightDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "/v1/adhesions")
public class AdhesionController {

    @Autowired
    private AdhesionService adhesionService;
    @Autowired
    private AdhesionLightService adhesionLightService;

    @Operation(summary = "Sauvegarde d'une adhésion")
    @PostMapping
    public ResponseEntity<AdhesionDto> createAdhesion(@RequestBody AdhesionDto adhesion) {
        adhesion = this.adhesionService.createAdhesion(adhesion);
        return ResponseEntity.ok(adhesion);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<AdhesionDto> updateAdhesion(@PathVariable("id") Long id, @RequestBody AdhesionDto adhesion) {
        adhesion = this.adhesionService.updateAdhesion(id, adhesion);
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

    @PatchMapping
    public ResponseEntity patchAdhesion(@RequestBody JsonNode patchesNode) {
        Set<Long> ids = this.adhesionService.patchAdhesions(patchesNode);
        return ResponseEntity.ok(ids);
    }

    @GetMapping
    public ResponseEntity<List<AdhesionLightDto>> findAdhesionsByCriteria(@ModelAttribute AdhesionCriteria criteria) {
        List<AdhesionLightDto> adhesions = this.adhesionLightService.findAdhesionsLightByCriteria(criteria);
        return ResponseEntity.ok(adhesions);
    }
}
