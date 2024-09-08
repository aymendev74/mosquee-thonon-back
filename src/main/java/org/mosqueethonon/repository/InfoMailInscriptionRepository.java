package org.mosqueethonon.repository;

import org.mosqueethonon.entity.InfoMailInscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoMailInscriptionRepository extends JpaRepository<InfoMailInscriptionEntity, Long> {

}
