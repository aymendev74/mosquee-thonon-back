package org.mosqueethonon.utilisateur.repository;

import org.mosqueethonon.utilisateur.entity.UtilisateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<UtilisateurEntity, Long>, JpaSpecificationExecutor<UtilisateurEntity> {

    public Optional<UtilisateurEntity> findByUsername(String username);

    public Optional<UtilisateurEntity> findByEmail(String email);

    public Optional<UtilisateurEntity> findFirstByEmail(String email);

}
