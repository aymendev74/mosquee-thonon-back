package org.mosqueethonon.v1.mapper.inscription;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mosqueethonon.entity.inscription.EleveEnrichedEntity;
import org.mosqueethonon.v1.dto.inscription.EleveEnrichedDto;

@Mapper(componentModel = "spring")
public interface EleveEnrichedMapper {

    EleveEnrichedDto fromEntityToDto(EleveEnrichedEntity eleveEntity);

    @InheritInverseConfiguration
    EleveEnrichedEntity fromDtoToEntity(EleveEnrichedDto eleveDto);

}
