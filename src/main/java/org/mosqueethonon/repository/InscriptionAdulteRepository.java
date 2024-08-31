package org.mosqueethonon.repository;

import org.mosqueethonon.entity.InscriptionAdulteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InscriptionAdulteRepository extends JpaRepository<InscriptionAdulteEntity, Long> {

}
