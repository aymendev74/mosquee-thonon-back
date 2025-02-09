package org.mosqueethonon.v1.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.mosqueethonon.service.bulletin.BulletinService;
import org.mosqueethonon.service.inscription.EleveService;
import org.mosqueethonon.v1.criterias.SearchEleveCriteria;
import org.mosqueethonon.v1.dto.bulletin.BulletinDto;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.dto.inscription.EleveEnrichedDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/eleves")
@AllArgsConstructor
public class EleveController {

    private EleveService eleveService;
    private BulletinService bulletinService;

    @GetMapping
    public ResponseEntity<List<EleveDto>> findElevesByCriteria(SearchEleveCriteria criteria) {
        return ResponseEntity.ok(this.eleveService.findElevesByCriteria(criteria));
    }

    @PatchMapping
    public ResponseEntity<List<EleveDto>> patchEleves(@RequestBody JsonNode patchesNode) {
        this.eleveService.patchEleves(patchesNode);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/enriched")
    public ResponseEntity<List<EleveEnrichedDto>> findEnrichedElevesByIdClasse(Long idClasse) {
        return ResponseEntity.ok(this.eleveService.findElevesEnrichedByIdClasse(idClasse));
    }

    @GetMapping(path = "/{id}/bulletins")
    public ResponseEntity<List<BulletinDto>> findBulletinsByEleveId(@PathVariable("id") Long idEleve) {
        return ResponseEntity.ok(this.bulletinService.findBulletinsByIdEleve(idEleve));
    }

}
