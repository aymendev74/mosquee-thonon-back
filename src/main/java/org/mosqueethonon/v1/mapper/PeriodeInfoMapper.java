package org.mosqueethonon.v1.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.PeriodeInfoEntity;
import org.mosqueethonon.v1.dto.PeriodeInfoDto;

@Mapper(componentModel = "spring")
public interface PeriodeInfoMapper {

    @Mapping(source = "dateDebut", target = "dateDebut", dateFormat = "dd.MM.yyyy")
    @Mapping(source = "dateFin", target = "dateFin", dateFormat = "dd.MM.yyyy")
    public PeriodeInfoEntity fromDtoToEntity(PeriodeInfoDto periode) ;

    @Mapping(source = "dateDebut", target = "dateDebut", dateFormat = "dd.MM.yyyy")
    @Mapping(source = "dateFin", target = "dateFin", dateFormat = "dd.MM.yyyy")
    public PeriodeInfoDto fromEntityToDto(PeriodeInfoEntity periode);

}
