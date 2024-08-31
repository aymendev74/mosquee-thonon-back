package org.mosqueethonon.repository;

import org.mosqueethonon.entity.InscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InscriptionRepository extends JpaRepository<InscriptionEntity, Long>, JpaSpecificationExecutor<InscriptionEntity> {

    @Query(value = "select nextval('moth.inscription_noinscinscription_seq')", nativeQuery = true)
    Long getNextNumeroInscription();

}
