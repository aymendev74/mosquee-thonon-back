package org.mosqueethonon.service.bulletin;

import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.v1.dto.bulletin.BulletinDto;

import java.util.List;
import java.util.Optional;

public interface BulletinService {

    List<BulletinDto> findBulletinsByIdEleve(Long idEleve);

    BulletinDto createBulletin(BulletinDto bulletinDto);

    BulletinDto updateBulletin(Long id, BulletinDto bulletinDto);

    void deleteBulletin(Long id);

    Optional<DocumentEntity> findDocumentByBulletinId(Long bulletinId);

}
