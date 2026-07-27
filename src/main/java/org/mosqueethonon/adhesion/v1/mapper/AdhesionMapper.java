package org.mosqueethonon.adhesion.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.adhesion.entity.AdhesionEntity;
import org.mosqueethonon.adhesion.v1.dto.AdhesionDto;

@Mapper(componentModel = "spring")
public interface AdhesionMapper {

    @Mapping(target = "id", ignore = true)
    public void updateAdhesion(AdhesionDto adhesionDto, @MappingTarget AdhesionEntity adhesionEntity) ;

    public AdhesionDto fromEntityToDto(AdhesionEntity adhesionEntity);

}
