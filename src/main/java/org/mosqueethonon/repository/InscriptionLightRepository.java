package org.mosqueethonon.repository;

import org.mosqueethonon.entity.InscriptionLightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InscriptionLightRepository extends JpaRepository<InscriptionLightEntity, Long>, JpaSpecificationExecutor<InscriptionLightEntity> {

}
