package org.mosqueethonon.repository;

import org.mosqueethonon.entity.MailingConfirmationEntity;
import org.mosqueethonon.enums.MailingConfirmationStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailingConfirmationRepository extends JpaRepository<MailingConfirmationEntity, Long> {

    List<MailingConfirmationEntity> findByStatut(MailingConfirmationStatut statut);

}
