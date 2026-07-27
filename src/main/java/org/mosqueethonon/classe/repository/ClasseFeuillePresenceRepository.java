package org.mosqueethonon.classe.repository;

import org.mosqueethonon.classe.entity.ClasseFeuillePresenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClasseFeuillePresenceRepository extends JpaRepository<ClasseFeuillePresenceEntity, Long> {

    ClasseFeuillePresenceEntity findByFeuillePresenceId(Long idFeuillePresence);

}
