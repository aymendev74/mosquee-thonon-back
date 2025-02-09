package org.mosqueethonon.repository;

import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulletinRepository extends JpaRepository<BulletinEntity, Long> {

    List<BulletinEntity> findByIdEleve(Long idEleve);

}
