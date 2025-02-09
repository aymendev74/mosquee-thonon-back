package org.mosqueethonon.service.bulletin;

import org.mosqueethonon.v1.dto.bulletin.BulletinDto;

import java.util.List;

public interface BulletinService {

    List<BulletinDto> findBulletinsByIdEleve(Long idEleve);

    BulletinDto createBulletin(BulletinDto bulletinDto);

    BulletinDto updateBulletin(Long id, BulletinDto bulletinDto);

}
