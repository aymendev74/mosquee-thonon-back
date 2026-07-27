package org.mosqueethonon.document.repository;

import org.mosqueethonon.document.entity.DocumentRequestEntity;
import org.mosqueethonon.document.enums.DocumentRequestStatutEnum;
import org.mosqueethonon.document.enums.DocumentRequestTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.transaction.Transactional;

@Repository
public interface DocumentRequestRepository extends JpaRepository<DocumentRequestEntity, Long> {

    List<DocumentRequestEntity> findByStatutOrderBySignatureDateCreationAsc(DocumentRequestStatutEnum statut);

    boolean existsByTypeAndBusinessIdAndStatut(DocumentRequestTypeEnum type, Long businessId, DocumentRequestStatutEnum statut);

    Optional<DocumentRequestEntity> findByTypeAndBusinessIdAndStatut(DocumentRequestTypeEnum type, Long businessId, DocumentRequestStatutEnum statut);

    @Transactional
    void deleteByTypeAndBusinessIdIn(DocumentRequestTypeEnum type, Set<Long> businessIds);

    /**
     * Récupère le premier enregistrement PENDING en le verrouillant de manière pessimiste
     * avec SKIP LOCKED (SELECT FOR UPDATE SKIP LOCKED).
     * Les enregistrements déjà verrouillés par une autre instance sont automatiquement ignorés,
     * ce qui garantit qu'un même enregistrement n'est jamais traité par deux instances en parallèle.
     * Doit être appelé dans une transaction active pour que le lock soit maintenu jusqu'au commit.
     */
    @Query(value = """
            SELECT * FROM moth.document_request
            WHERE cddorestatut = 'PENDING'
            ORDER BY oh_date_cre ASC
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    Optional<DocumentRequestEntity> findFirstPendingWithLock();

}
