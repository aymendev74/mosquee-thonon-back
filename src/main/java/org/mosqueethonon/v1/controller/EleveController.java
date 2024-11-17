package org.mosqueethonon.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.service.inscription.EleveService;
import org.mosqueethonon.v1.criterias.SearchEleveCriteria;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/eleves")
@AllArgsConstructor
public class EleveController {

    private EleveService eleveService;

    @GetMapping
    public ResponseEntity<List<EleveDto>> findElevesByCriteria(SearchEleveCriteria criteria) {
        return ResponseEntity.ok(this.eleveService.findElevesByCriteria(criteria));
    }

}
