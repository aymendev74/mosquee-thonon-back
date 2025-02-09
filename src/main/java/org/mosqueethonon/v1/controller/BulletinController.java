package org.mosqueethonon.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.service.bulletin.BulletinService;
import org.mosqueethonon.v1.dto.bulletin.BulletinDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1/bulletins")
@AllArgsConstructor
public class BulletinController {

    private BulletinService bulletinService;

    @PostMapping
    public ResponseEntity<BulletinDto> createBulletin(@RequestBody BulletinDto bulletin) {
        return ResponseEntity.ok(this.bulletinService.createBulletin(bulletin));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BulletinDto> updateBulletin(@PathVariable Long id, @RequestBody BulletinDto bulletin) {
        return ResponseEntity.ok(this.bulletinService.updateBulletin(id, bulletin));
    }

}
