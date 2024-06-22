package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.IClasseService;
import org.mosqueethonon.v1.criterias.CreateClasseCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1/classes")
public class ClasseController {

    @Autowired
    private IClasseService classeService;

    @PostMapping
    public ResponseEntity<Void> createClasse(@RequestBody CreateClasseCriteria criteria) {
        this.classeService.createClasses(criteria);
        return ResponseEntity.ok().build();
    }
}
