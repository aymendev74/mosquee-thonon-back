package org.mosqueethonon.repository;

import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.DocumentRequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.transaction.Transactional;

@Repository
public interface DocumentRequestRepository extends JpaRepository<DocumentRequestEntity, Long> {

    List<DocumentRequestEntity> findByStatutOrderBySignatureDateCreationAsc(DocumentRequestStatut statut);

    boolean existsByTypeAndBusinessIdAndStatut(DocumentRequestType type, Long businessId, DocumentRequestStatut statut);

    Optional<DocumentRequestEntity> findByTypeAndBusinessIdAndStatut(DocumentRequestType type, Long businessId, DocumentRequestStatut statut);

    @Transactional
    void deleteByTypeAndBusinessIdIn(DocumentRequestType type, Set<Long> businessIds);

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
