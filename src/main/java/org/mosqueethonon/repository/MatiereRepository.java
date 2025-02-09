package org.mosqueethonon.repository;

import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatiereRepository extends JpaRepository<MatiereEntity, Long> {

}
