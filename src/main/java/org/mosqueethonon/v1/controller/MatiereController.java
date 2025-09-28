package org.mosqueethonon.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.enums.TypeMatiereEnum;
import org.mosqueethonon.service.referentiel.MatiereService;
import org.mosqueethonon.v1.dto.referentiel.MatiereDto;
import org.mosqueethonon.v1.dto.referentiel.TraductionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/v1/matieres")
public class MatiereController {

    private MatiereService matiereService;

    @GetMapping
    public ResponseEntity<Map<TypeMatiereEnum, List<TraductionDto>>> findAllMatieres() {
        return ResponseEntity.ok(this.matiereService.findAll());
    }

}
