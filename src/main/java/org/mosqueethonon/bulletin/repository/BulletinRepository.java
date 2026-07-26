package org.mosqueethonon.bulletin.repository;

import org.mosqueethonon.bulletin.entity.BulletinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulletinRepository extends JpaRepository<BulletinEntity, Long> {

    List<BulletinEntity> findByIdEleve(Long idEleve);

    List<BulletinEntity> findByIdEleveIn(List<Long> eleveIds);

}
