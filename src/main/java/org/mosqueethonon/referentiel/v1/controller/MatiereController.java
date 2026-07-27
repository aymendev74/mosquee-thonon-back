package org.mosqueethonon.referentiel.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.referentiel.enums.TypeMatiereEnum;
import org.mosqueethonon.referentiel.service.MatiereService;
import org.mosqueethonon.referentiel.v1.dto.MatiereDto;
import org.mosqueethonon.referentiel.v1.dto.TraductionDto;
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
