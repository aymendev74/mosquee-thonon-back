package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.AdhesionService;
import org.mosqueethonon.service.InscriptionService;
import org.mosqueethonon.v1.dto.AdhesionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/adhesions")
@CrossOrigin
public class AdhesionController {

    @Autowired
    private AdhesionService adhesionService;

    @PostMapping
    public ResponseEntity<AdhesionDto> saveAdhesion(@RequestBody AdhesionDto adhesion) {
        adhesion = this.adhesionService.saveAdhesion(adhesion);
        return ResponseEntity.ok(adhesion);
    }

}
