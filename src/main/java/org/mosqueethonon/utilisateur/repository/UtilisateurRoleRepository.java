package org.mosqueethonon.utilisateur.repository;

import org.mosqueethonon.utilisateur.entity.UtilisateurRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRoleEntity, Long> {

}
