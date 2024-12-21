package org.mosqueethonon.repository;

import org.mosqueethonon.entity.inscription.EleveEnrichedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EleveEnrichedRepository extends JpaRepository<EleveEnrichedEntity, Long> {

    List<EleveEnrichedEntity> findByIdClasse(Long idClasse);

}
