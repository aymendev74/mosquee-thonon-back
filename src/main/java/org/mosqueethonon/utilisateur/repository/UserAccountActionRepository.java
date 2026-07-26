package org.mosqueethonon.utilisateur.repository;

import org.mosqueethonon.utilisateur.entity.UserAccountActionEntity;
import org.mosqueethonon.mail.enums.MailRequestStatutEnum;
import org.mosqueethonon.utilisateur.enums.UserAccountActionTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAccountActionRepository extends JpaRepository<UserAccountActionEntity, Long> {

    List<UserAccountActionEntity> findByStatutAndTypeOrderBySignatureDateCreationAsc(MailRequestStatutEnum statut, UserAccountActionTypeEnum type);

    UserAccountActionEntity findByTokenAndType(String token, UserAccountActionTypeEnum type);

    void deleteByUsernameAndType(String username, UserAccountActionTypeEnum type);

    void deleteByUsername(String username);

}
