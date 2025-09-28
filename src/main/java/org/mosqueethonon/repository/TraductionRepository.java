package org.mosqueethonon.repository;

import org.mosqueethonon.entity.referentiel.TraductionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraductionRepository extends JpaRepository<TraductionEntity, Long> {

    Optional<TraductionEntity> findByCleAndValeur(String cle, String valeur);

}
