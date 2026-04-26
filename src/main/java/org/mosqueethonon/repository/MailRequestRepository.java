package org.mosqueethonon.repository;

import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.enums.MailRequestType;
import org.mosqueethonon.enums.MailRequestStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface MailRequestRepository extends JpaRepository<MailRequestEntity, Long> {

    List<MailRequestEntity> findByStatutOrderBySignatureDateCreationAsc(MailRequestStatut statut);

    @Query("SELECT m FROM MailRequestEntity m LEFT JOIN FETCH m.documentRequests WHERE m.statut = :statut ORDER BY m.signature.dateCreation ASC")
    List<MailRequestEntity> findByStatutWithDocumentsOrderBySignatureDateCreationAsc(@Param("statut") MailRequestStatut statut);

    @Transactional
    void deleteByTypeAndBusinessIdIn(MailRequestType type, Set<Long> businessIds);
}
