package org.mosqueethonon.repository;

import org.mosqueethonon.entity.classe.FeuillePresenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeuillePresenceRepository extends JpaRepository<FeuillePresenceEntity, Long> {

}
