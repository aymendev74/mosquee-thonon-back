package org.mosqueethonon.repository;

import org.mosqueethonon.entity.AdhesionLightEntity;
import org.mosqueethonon.entity.InscriptionLightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AdhesionLightRepository extends JpaRepository<AdhesionLightEntity, Long>, JpaSpecificationExecutor<AdhesionLightEntity> {

}