package org.mosqueethonon.utilisateur.repository;

import org.mosqueethonon.utilisateur.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {


}
