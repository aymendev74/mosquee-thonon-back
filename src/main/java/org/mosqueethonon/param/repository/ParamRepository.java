package org.mosqueethonon.param.repository;

import org.mosqueethonon.param.entity.ParamEntity;
import org.mosqueethonon.param.enums.ParamNameEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParamRepository extends JpaRepository<ParamEntity, Long> {

    ParamEntity findByName(ParamNameEnum paramNameEnum);

}
