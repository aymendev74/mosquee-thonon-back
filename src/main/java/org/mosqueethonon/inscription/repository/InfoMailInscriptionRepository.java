package org.mosqueethonon.inscription.repository;

import org.mosqueethonon.inscription.entity.InfoMailInscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoMailInscriptionRepository extends JpaRepository<InfoMailInscriptionEntity, Long> {

}
