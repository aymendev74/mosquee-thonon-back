package org.mosqueethonon.referentiel.v1.mapper;


import org.mapstruct.Mapper;
import org.mosqueethonon.referentiel.entity.PeriodeInfoEntity;
import org.mosqueethonon.referentiel.v1.dto.PeriodeInfoDto;

@Mapper(componentModel = "spring")
public interface PeriodeInfoMapper {

    public PeriodeInfoEntity fromDtoToEntity(PeriodeInfoDto periode) ;

    public PeriodeInfoDto fromEntityToDto(PeriodeInfoEntity periode);

}
