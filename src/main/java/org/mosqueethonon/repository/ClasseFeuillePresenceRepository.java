package org.mosqueethonon.repository;

import org.mosqueethonon.entity.classe.ClasseFeuillePresenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClasseFeuillePresenceRepository extends JpaRepository<ClasseFeuillePresenceEntity, Long> {

}
