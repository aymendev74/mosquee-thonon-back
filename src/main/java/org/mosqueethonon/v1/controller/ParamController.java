package org.mosqueethonon.v1.controller;

import org.mosqueethonon.service.ParamService;
import org.mosqueethonon.v1.dto.ParamDto;
import org.mosqueethonon.v1.dto.ParamsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/v1/params")
public class ParamController {

    @Autowired
    private ParamService paramService;

    @GetMapping("/reinscription-enabled")
    public ResponseEntity<Boolean> isReinscriptionEnabled() {
        boolean isReinscriptionEnabled = this.paramService.isReinscriptionPrioritaireEnabled();
        return ResponseEntity.ok(isReinscriptionEnabled);
    }

    @GetMapping
    public ResponseEntity<ParamsDto> getParams() {
        ParamsDto paramsDto = this.paramService.getParams();
        return ResponseEntity.ok(paramsDto);
    }

    @PostMapping
    public ResponseEntity saveParam(@RequestBody List<ParamDto> params) {
        this.paramService.saveParam(params);
        return ResponseEntity.ok().build();
    }
}
