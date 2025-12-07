package org.mosqueethonon.repository;

import org.mosqueethonon.entity.mail.MailingActivationUtilisateurEntity;
import org.mosqueethonon.enums.MailingStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailingActivationUtilisateurRepository extends JpaRepository<MailingActivationUtilisateurEntity, Long> {

    List<MailingActivationUtilisateurEntity> findByStatut(MailingStatut statut);

    MailingActivationUtilisateurEntity findByToken(String token);

    void deleteByUsername(String username);

}
