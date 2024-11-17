package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.IClasseService;
import org.mosqueethonon.v1.criterias.CreateClasseCriteria;
import org.mosqueethonon.v1.criterias.SearchClasseCriteria;
import org.mosqueethonon.v1.dto.classe.ClasseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/classes")
public class ClasseController {

    @Autowired
    private IClasseService classeService;

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

}
