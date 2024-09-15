package org.mosqueethonon.repository;

import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<UtilisateurEntity, Long> {

    public Optional<UtilisateurEntity> findByUsername(String username);

}
