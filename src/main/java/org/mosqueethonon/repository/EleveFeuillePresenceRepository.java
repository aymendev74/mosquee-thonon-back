package org.mosqueethonon.repository;

import org.mosqueethonon.entity.classe.EleveFeuillePresenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EleveFeuillePresenceRepository extends JpaRepository<EleveFeuillePresenceEntity, Long> {

    @Modifying
    @Query("DELETE FROM EleveFeuillePresenceEntity e WHERE e.idEleve IN :eleveIds")
    void deleteByEleveIdIn(@Param("eleveIds") List<Long> eleveIds);

}
