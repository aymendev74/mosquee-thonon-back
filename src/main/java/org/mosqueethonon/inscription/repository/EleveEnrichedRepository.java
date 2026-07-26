package org.mosqueethonon.inscription.repository;

import org.mosqueethonon.inscription.entity.EleveEnrichedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EleveEnrichedRepository extends JpaRepository<EleveEnrichedEntity, Long> {

    List<EleveEnrichedEntity> findByIdClasseOrderByNomAscPrenomAsc(Long idClasse);

}
