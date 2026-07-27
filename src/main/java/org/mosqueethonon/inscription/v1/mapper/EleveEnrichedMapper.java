package org.mosqueethonon.inscription.v1.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mosqueethonon.inscription.entity.EleveEnrichedEntity;
import org.mosqueethonon.inscription.v1.dto.EleveEnrichedDto;

@Mapper(componentModel = "spring")
public interface EleveEnrichedMapper {

    EleveEnrichedDto fromEntityToDto(EleveEnrichedEntity eleveEntity);

    @InheritInverseConfiguration
    EleveEnrichedEntity fromDtoToEntity(EleveEnrichedDto eleveDto);

}
