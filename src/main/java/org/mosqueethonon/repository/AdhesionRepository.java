package org.mosqueethonon.repository;

import org.mosqueethonon.entity.AdhesionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdhesionRepository extends JpaRepository<AdhesionEntity, Long> {

}
