package org.mosqueethonon.classe.repository;

import org.mosqueethonon.classe.entity.FeuillePresenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeuillePresenceRepository extends JpaRepository<FeuillePresenceEntity, Long> {

}
