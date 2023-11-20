package org.mosqueethonon.v1.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.PeriodeEntity;
import org.mosqueethonon.v1.dto.PeriodeDto;

@Mapper(componentModel = "spring")
public interface PeriodeMapper {

    @Mapping(source = "dateDebut", target = "dateDebut", dateFormat = "dd.MM.yyyy")
    @Mapping(source = "dateFin", target = "dateFin", dateFormat = "dd.MM.yyyy")
    public PeriodeEntity fromDtoToEntity(PeriodeDto periode) ;

    @Mapping(source = "dateDebut", target = "dateDebut", dateFormat = "dd.MM.yyyy")
    @Mapping(source = "dateFin", target = "dateFin", dateFormat = "dd.MM.yyyy")
    public PeriodeDto fromEntityToDto(PeriodeEntity periode);

}
