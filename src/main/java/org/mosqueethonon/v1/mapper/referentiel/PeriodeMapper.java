package org.mosqueethonon.v1.mapper.referentiel;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;

@Mapper(componentModel = "spring")
public interface PeriodeMapper {

    public PeriodeEntity mapDtoToEntity(PeriodeDto periode, @MappingTarget PeriodeEntity periodeEntity) ;

    public PeriodeDto fromEntityToDto(PeriodeEntity periode);

}
