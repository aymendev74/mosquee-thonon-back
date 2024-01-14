package org.mosqueethonon.repository;

import org.mosqueethonon.entity.PeriodeInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodeInfoRepository extends JpaRepository<PeriodeInfoEntity, Long> {

}
