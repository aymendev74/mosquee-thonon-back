package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.PeriodeEntity;
import org.mosqueethonon.v1.dto.PeriodeDto;

@Mapper(componentModel = "spring")
public interface PeriodeMapper {

    public PeriodeEntity fromDtoToEntity(PeriodeDto periode) ;

    public PeriodeDto fromEntityToDto(PeriodeEntity periode);

}
