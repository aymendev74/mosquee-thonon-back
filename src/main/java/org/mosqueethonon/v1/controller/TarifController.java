package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.TarifService;
import org.mosqueethonon.service.criteria.TarifCriteria;
import org.mosqueethonon.v1.dto.TarifDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "api/v1/tarifs")
public class TarifController {

    @Autowired
    private TarifService tarifService;

    @GetMapping
    public ResponseEntity<List<TarifDto>> findTarifs(@ModelAttribute TarifCriteria criteria) {
        List<TarifDto> tarifs = this.tarifService.findTarifByCriteria(criteria);
        return ResponseEntity.ok(tarifs);
    }
}
