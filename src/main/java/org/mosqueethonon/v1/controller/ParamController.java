package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.ParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1/params")
@CrossOrigin
public class ParamController {

    @Autowired
    private ParamService paramService;

    @GetMapping("/reinscription-enabled")
    public ResponseEntity<Boolean> isReinscriptionEnabled() {
        boolean isReinscriptionEnabled = this.paramService.isReinscriptionPrioritaireEnabled();
        return ResponseEntity.ok(isReinscriptionEnabled);
    }
}
