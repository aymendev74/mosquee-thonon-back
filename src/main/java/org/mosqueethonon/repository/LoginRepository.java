package org.mosqueethonon.repository;

import org.mosqueethonon.entity.utilisateur.LoginHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginRepository extends JpaRepository<LoginHistoryEntity, Long> {

    void deleteByUsername(String username);

}
