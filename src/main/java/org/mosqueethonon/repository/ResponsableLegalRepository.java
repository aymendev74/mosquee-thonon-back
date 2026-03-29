package org.mosqueethonon.repository;

import org.mosqueethonon.entity.inscription.ResponsableLegalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResponsableLegalRepository extends JpaRepository<ResponsableLegalEntity, Long> {


}
