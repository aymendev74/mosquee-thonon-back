package org.mosqueethonon.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.service.enseignant.EnseignantService;
import org.mosqueethonon.v1.dto.enseignant.EnseignantDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/enseignants")
@AllArgsConstructor
public class EnseignantController {

    private EnseignantService enseignantService;

    @GetMapping
    public ResponseEntity<List<EnseignantDto>> getEnseignants() {
        return ResponseEntity.ok(this.enseignantService.findAllEnseignants());
    }

    @PostMapping
    public ResponseEntity<EnseignantDto> createEnseignant(@RequestBody EnseignantDto enseignantDto) {
        return ResponseEntity.ok(this.enseignantService.createEnseignantDto(enseignantDto));
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<EnseignantDto> updateEnseignant(@PathVariable("id") Long id, @RequestBody EnseignantDto enseignantDto) {
        return ResponseEntity.ok(this.enseignantService.updateEnseignantDto(id, enseignantDto));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Boolean> deleteEnseignant(@PathVariable("id") Long id) {
        return ResponseEntity.ok(this.enseignantService.deleteEnseignant(id));
    }

}
