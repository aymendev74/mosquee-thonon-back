package org.mosqueethonon.repository;

import org.mosqueethonon.entity.utilisateur.UtilisateurRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRoleEntity, Long> {

}
