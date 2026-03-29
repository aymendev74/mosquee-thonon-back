package org.mosqueethonon.repository;

import org.mosqueethonon.entity.mail.UserAccountActionEntity;
import org.mosqueethonon.enums.MailRequestStatut;
import org.mosqueethonon.enums.UserAccountActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAccountActionRepository extends JpaRepository<UserAccountActionEntity, Long> {

    List<UserAccountActionEntity> findByStatutAndTypeOrderBySignatureDateCreationAsc(MailRequestStatut statut, UserAccountActionType type);

    UserAccountActionEntity findByTokenAndType(String token, UserAccountActionType type);

    void deleteByUsernameAndType(String username, UserAccountActionType type);

    void deleteByUsername(String username);

}
