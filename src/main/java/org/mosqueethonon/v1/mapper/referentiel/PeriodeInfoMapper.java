package org.mosqueethonon.v1.mapper.referentiel;


import org.mapstruct.Mapper;
import org.mosqueethonon.entity.referentiel.PeriodeInfoEntity;
import org.mosqueethonon.v1.dto.referentiel.PeriodeInfoDto;

@Mapper(componentModel = "spring")
public interface PeriodeInfoMapper {

    public PeriodeInfoEntity fromDtoToEntity(PeriodeInfoDto periode) ;

    public PeriodeInfoDto fromEntityToDto(PeriodeInfoEntity periode);

}
