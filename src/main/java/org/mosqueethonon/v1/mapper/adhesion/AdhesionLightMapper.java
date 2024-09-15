package org.mosqueethonon.v1.mapper.adhesion;

import org.mapstruct.Mapper;
import org.mosqueethonon.entity.adhesion.AdhesionLightEntity;
import org.mosqueethonon.v1.dto.adhesion.AdhesionLightDto;

@Mapper(componentModel = "spring")
public interface AdhesionLightMapper {

    public AdhesionLightEntity fromDtoToEntity(AdhesionLightDto adhesionDto) ;

    public AdhesionLightDto fromEntityToDto(AdhesionLightEntity adhesionEntity);


}
