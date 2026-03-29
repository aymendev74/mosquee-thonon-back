package org.mosqueethonon.repository;

import org.mosqueethonon.entity.classe.LienClasseEleveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LienClasseEleveRepository extends JpaRepository<LienClasseEleveEntity, Long> {

    @Modifying
    @Query("DELETE FROM LienClasseEleveEntity l WHERE l.eleve.id IN :eleveIds")
    void deleteByEleveIdIn(@Param("eleveIds") List<Long> eleveIds);

}
