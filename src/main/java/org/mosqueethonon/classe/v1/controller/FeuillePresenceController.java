package org.mosqueethonon.classe.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.classe.service.FeuillePresenceService;
import org.mosqueethonon.classe.v1.dto.FeuillePresenceDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/v1/presences")
@AllArgsConstructor
public class FeuillePresenceController {

    private FeuillePresenceService feuillePresenceService;

    @PutMapping("/{id}")
    public ResponseEntity<FeuillePresenceDto> updateFeuillePresence(@PathVariable("id") Long id, @RequestBody FeuillePresenceDto feuillePresence) {
        feuillePresence = this.feuillePresenceService.updateFeuillePresence(id, feuillePresence);
        return ResponseEntity.ok(feuillePresence);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeuillePresence(@PathVariable("id") Long id) {
        this.feuillePresenceService.deleteFeuillePresence(id);
        return ResponseEntity.ok().build();
    }

}
