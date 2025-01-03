package org.mosqueethonon.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.service.classe.IClasseService;
import org.mosqueethonon.service.classe.IFeuillePresenceService;
import org.mosqueethonon.v1.criterias.CreateClasseCriteria;
import org.mosqueethonon.v1.criterias.SearchClasseCriteria;
import org.mosqueethonon.v1.dto.classe.ClasseDto;
import org.mosqueethonon.v1.dto.classe.FeuillePresenceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/classes")
@AllArgsConstructor
public class ClasseController {

    private IClasseService classeService;

    private IFeuillePresenceService feuillePresenceService;

    @PostMapping("/auto")
    public ResponseEntity<Void> createClasse(@RequestBody CreateClasseCriteria criteria) {
        this.classeService.createClasses(criteria);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<ClasseDto> createClasse(@RequestBody ClasseDto classe) {
        return ResponseEntity.ok(this.classeService.createClasse(classe));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClasseDto> updateClasse(@PathVariable("id") Long id,@RequestBody ClasseDto classe) {
        return ResponseEntity.ok(this.classeService.updateClasse(id, classe));
    }

    @GetMapping
    public ResponseEntity<List<ClasseDto>> getClasses(@ModelAttribute SearchClasseCriteria criteria) {
        return ResponseEntity.ok(this.classeService.findClassesByCriteria(criteria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClasseDto> findClasseById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(this.classeService.findClasseById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClasse(@PathVariable("id") Long id) {
        this.classeService.deleteClasse(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/presences")
    public ResponseEntity<FeuillePresenceDto> createFeuillePresence(@PathVariable("id") Long idClasse, @RequestBody FeuillePresenceDto feuillePresence) {
        feuillePresence = this.feuillePresenceService.createFeuillePresence(idClasse, feuillePresence);
        return ResponseEntity.ok(feuillePresence);
    }

    @GetMapping("/{id}/presences")
    public ResponseEntity<List<FeuillePresenceDto>> findFeuillesPresenceByClasseId(@PathVariable("id") Long idClasse) {
        List<FeuillePresenceDto> feuillesPresence = this.feuillePresenceService.findFeuillePresencesByClasseId(idClasse);
        return ResponseEntity.ok(feuillesPresence);
    }

}
