package org.mosqueethonon.v1.mapper.inscription;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.v1.dto.inscription.EleveDto;

@Mapper(componentModel = "spring")
public interface EleveMapper {

    public EleveEntity fromDtoToEntity(EleveDto eleveDto) ;

    public EleveDto fromEntityToDto(EleveEntity eleveEntity);

}
