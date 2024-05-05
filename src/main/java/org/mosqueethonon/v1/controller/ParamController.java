package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.ParamService;
import org.mosqueethonon.v1.dto.ParamDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/v1/params")
public class ParamController {

    @Autowired
    private ParamService paramService;

    @GetMapping("/reinscription-enabled")
    public ResponseEntity<Boolean> isReinscriptionEnabled() {
        boolean isReinscriptionEnabled = this.paramService.isReinscriptionPrioritaireEnabled();
        return ResponseEntity.ok(isReinscriptionEnabled);
    }

    @PostMapping
    public ResponseEntity saveParam(@RequestBody ParamDto param) {
        this.paramService.saveParam(param);
        return ResponseEntity.ok().build();
    }
}
