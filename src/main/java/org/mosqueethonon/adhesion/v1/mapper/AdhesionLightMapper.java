package org.mosqueethonon.adhesion.v1.mapper;

import org.mapstruct.Mapper;
import org.mosqueethonon.adhesion.entity.AdhesionLightEntity;
import org.mosqueethonon.adhesion.v1.dto.AdhesionLightDto;

@Mapper(componentModel = "spring")
public interface AdhesionLightMapper {

    public AdhesionLightEntity fromDtoToEntity(AdhesionLightDto adhesionDto) ;

    public AdhesionLightDto fromEntityToDto(AdhesionLightEntity adhesionEntity);


}
