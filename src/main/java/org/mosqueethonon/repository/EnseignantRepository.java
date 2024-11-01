package org.mosqueethonon.repository;

import org.mosqueethonon.entity.classe.EnseignantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnseignantRepository extends JpaRepository<EnseignantEntity, Long> {

}
