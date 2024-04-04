package org.mosqueethonon.repository;

import org.mosqueethonon.entity.PeriodeInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeriodeInfoRepository extends JpaRepository<PeriodeInfoEntity, Long> {

    List<PeriodeInfoEntity> findByApplicationOrderByDateDebutDesc(String application);

}
