package org.mosqueethonon.adhesion.repository;

import org.mosqueethonon.adhesion.entity.AdhesionLightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AdhesionLightRepository extends JpaRepository<AdhesionLightEntity, Long>, JpaSpecificationExecutor<AdhesionLightEntity> {

}
