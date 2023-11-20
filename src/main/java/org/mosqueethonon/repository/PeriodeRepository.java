package org.mosqueethonon.repository;

import org.mosqueethonon.entity.PeriodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodeRepository extends JpaRepository<PeriodeEntity, Long> {

}
