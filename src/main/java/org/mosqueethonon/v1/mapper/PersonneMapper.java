package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.PersonneEntity;
import org.mosqueethonon.v1.dto.PersonneDto;

@Mapper(componentModel = "spring")
public interface PersonneMapper {

    @Mapping(source = "dateNaissance", target = "dateNaissance", dateFormat = "dd.MM.yyyy")
    public PersonneEntity fromDtoToEntity(PersonneDto personneDto) ;
    @Mapping(source = "dateNaissance", target = "dateNaissance", dateFormat = "dd.MM.yyyy")
    public PersonneDto fromEntityToDto(PersonneEntity personneEntity);

}
