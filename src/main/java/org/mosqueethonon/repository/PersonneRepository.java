package org.mosqueethonon.repository;

import org.mosqueethonon.entity.PersonneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonneRepository extends JpaRepository<PersonneEntity, Long> {


}
