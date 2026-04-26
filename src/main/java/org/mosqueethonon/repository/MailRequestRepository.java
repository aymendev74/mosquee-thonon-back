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
import java.util.Optional;
import java.util.Set;

@Repository
public interface MailRequestRepository extends JpaRepository<MailRequestEntity, Long> {

    List<MailRequestEntity> findByStatutOrderBySignatureDateCreationAsc(MailRequestStatut statut);

    @Query("SELECT m FROM MailRequestEntity m LEFT JOIN FETCH m.documentRequests WHERE m.statut = :statut ORDER BY m.signature.dateCreation ASC")
    List<MailRequestEntity> findByStatutWithDocumentsOrderBySignatureDateCreationAsc(@Param("statut") MailRequestStatut statut);

    @Transactional
    void deleteByTypeAndBusinessIdIn(MailRequestType type, Set<Long> businessIds);

    /**
     * Récupère le premier enregistrement PENDING en le verrouillant de manière pessimiste
     * avec SKIP LOCKED (SELECT FOR UPDATE SKIP LOCKED).
     * Les enregistrements déjà verrouillés par une autre instance sont automatiquement ignorés,
     * ce qui garantit qu'un même enregistrement n'est jamais traité par deux instances en parallèle.
     * Doit être appelé dans une transaction active pour que le lock soit maintenu jusqu'au commit.
     * Note : les document_requests liés sont chargés séparément dans le service pour éviter
     * le mélange JPQL/SQL natif sur une requête avec JOIN FETCH.
     */
    @Query(value = """
            SELECT * FROM moth.mail_request
            WHERE cdmarestatut = 'PENDING'
            ORDER BY oh_date_cre ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<MailRequestEntity> findFirstPendingWithLock();

}
