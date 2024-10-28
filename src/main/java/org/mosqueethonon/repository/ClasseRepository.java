package org.mosqueethonon.repository;

import org.mosqueethonon.entity.classe.ClasseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClasseRepository extends JpaRepository<ClasseEntity, Long> {

}
