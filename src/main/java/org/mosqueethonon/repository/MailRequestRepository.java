package org.mosqueethonon.repository;

import org.mosqueethonon.entity.mail.MailRequestEntity;
import org.mosqueethonon.enums.MailRequestType;
import org.mosqueethonon.enums.MailRequestStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.transaction.Transactional;

@Repository
public interface MailRequestRepository extends JpaRepository<MailRequestEntity, Long> {

    /**
     * Trouve toutes les demandes par statut
     * @param statut Le statut des demandes à rechercher
     * @return Une liste de demandes correspondant au statut
     */
    List<MailRequestEntity> findByStatutOrderBySignatureDateCreationAsc(MailRequestStatut statut);

    /**
     * Supprime une demande par son type et son ID métier
     * @param type Le type de la demande (INSCRIPTION/ADHESION)
     * @param businessId L'ID métier (idInscription ou idAdhesion)
     */
    @Transactional
    void deleteByTypeAndBusinessIdIn(MailRequestType type, Set<Long> businessIds);
}
