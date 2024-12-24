package org.mosqueethonon.repository;

import org.mosqueethonon.entity.utilisateur.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {


}
