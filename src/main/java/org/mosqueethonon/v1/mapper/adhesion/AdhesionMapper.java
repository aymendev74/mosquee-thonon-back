package org.mosqueethonon.v1.mapper.adhesion;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.entity.adhesion.AdhesionEntity;
import org.mosqueethonon.v1.dto.adhesion.AdhesionDto;

@Mapper(componentModel = "spring")
public interface AdhesionMapper {

    @Mapping(target = "id", ignore = true)
    public void mapDtoToEntity(AdhesionDto adhesionDto, @MappingTarget AdhesionEntity adhesionEntity) ;

    public AdhesionDto fromEntityToDto(AdhesionEntity adhesionEntity);

}
