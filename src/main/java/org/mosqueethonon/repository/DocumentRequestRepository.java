package org.mosqueethonon.repository;

import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.DocumentRequestType;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
