package org.mosqueethonon.v1.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.PeriodeInfoEntity;
import org.mosqueethonon.v1.dto.PeriodeInfoDto;

@Mapper(componentModel = "spring")
public interface PeriodeInfoMapper {

    public PeriodeInfoEntity fromDtoToEntity(PeriodeInfoDto periode) ;

    public PeriodeInfoDto fromEntityToDto(PeriodeInfoEntity periode);

}
