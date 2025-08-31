package org.mosqueethonon.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.service.referentiel.MatiereService;
import org.mosqueethonon.v1.dto.referentiel.MatiereDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/matieres")
public class MatiereController {

    private MatiereService matiereService;

    @GetMapping
    public ResponseEntity<List<MatiereDto>> getAllMatieres() {
        return ResponseEntity.ok(this.matiereService.findAllMatieres());
    }

}
