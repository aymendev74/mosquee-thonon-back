package org.mosqueethonon.v1.controller;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.service.bulletin.BulletinService;
import org.mosqueethonon.service.document.DocumentService;
import org.mosqueethonon.v1.dto.bulletin.BulletinDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1/bulletins")
@AllArgsConstructor
public class BulletinController {

    private BulletinService bulletinService;

    private DocumentService documentService;

    @PostMapping
    public ResponseEntity<BulletinDto> createBulletin(@RequestBody BulletinDto bulletin) {
        return ResponseEntity.ok(this.bulletinService.createBulletin(bulletin));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BulletinDto> updateBulletin(@PathVariable Long id, @RequestBody BulletinDto bulletin) {
        return ResponseEntity.ok(this.bulletinService.updateBulletin(id, bulletin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBulletin(@PathVariable Long id) {
        this.bulletinService.deleteBulletin(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getBulletinPdf(@PathVariable Long id) {
        DocumentEntity document = this.bulletinService.findDocumentByBulletinId(id)
                .orElse(null);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] content = this.documentService.getDocumentContent(document.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", document.getLibelle());
        headers.setContentLength(content.length);

        return ResponseEntity.ok().headers(headers).body(content);
    }

}
