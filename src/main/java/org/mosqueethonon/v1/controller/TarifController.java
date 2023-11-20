package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.TarifService;
import org.mosqueethonon.v1.dto.TarifDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "v1/tarifs")
@CrossOrigin
public class TarifController {

    @Autowired
    private TarifService tarifService;

    @GetMapping
    public ResponseEntity<List<TarifDto>> findTarifs() {
        List<TarifDto> tarifs = this.tarifService.findAllTarifs();
        return ResponseEntity.ok(tarifs);
    }
}
