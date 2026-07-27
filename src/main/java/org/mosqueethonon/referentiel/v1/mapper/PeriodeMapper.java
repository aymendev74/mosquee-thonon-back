package org.mosqueethonon.referentiel.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.referentiel.entity.PeriodeEntity;
import org.mosqueethonon.referentiel.v1.dto.PeriodeDto;

@Mapper(componentModel = "spring")
public interface PeriodeMapper {

    public PeriodeEntity mapDtoToEntity(PeriodeDto periode, @MappingTarget PeriodeEntity periodeEntity) ;

    public PeriodeDto fromEntityToDto(PeriodeEntity periode);

}
