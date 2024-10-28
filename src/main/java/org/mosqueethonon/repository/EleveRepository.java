package org.mosqueethonon.repository;

import org.mosqueethonon.entity.inscription.EleveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EleveRepository extends JpaRepository<EleveEntity, Long> {

}
