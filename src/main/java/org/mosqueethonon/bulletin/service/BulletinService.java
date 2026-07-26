package org.mosqueethonon.bulletin.service;

import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.bulletin.v1.dto.BulletinDto;

import java.util.List;
import java.util.Optional;

public interface BulletinService {

    List<BulletinDto> findBulletinsByIdEleve(Long idEleve);

    BulletinDto createBulletin(BulletinDto bulletinDto);

    BulletinDto updateBulletin(Long id, BulletinDto bulletinDto);

    void deleteBulletin(Long id);

    Optional<DocumentEntity> findDocumentByBulletinId(Long bulletinId);

    boolean verifierCompletude(BulletinDto bulletinDto);

}
