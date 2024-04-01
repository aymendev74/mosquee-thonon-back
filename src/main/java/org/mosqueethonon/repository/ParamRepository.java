package org.mosqueethonon.repository;

import org.mosqueethonon.entity.ParamEntity;
import org.mosqueethonon.enums.ParamNameEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParamRepository extends JpaRepository<ParamEntity, Long> {

    ParamEntity findByName(ParamNameEnum paramNameEnum);

}
