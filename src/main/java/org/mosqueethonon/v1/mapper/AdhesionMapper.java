package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.entity.AdhesionEntity;
import org.mosqueethonon.v1.dto.AdhesionDto;

@Mapper(componentModel = "spring", uses = { SignatureMapper.class })
public interface AdhesionMapper {

    public void mapDtoToEntity(AdhesionDto adhesionDto, @MappingTarget AdhesionEntity adhesionEntity) ;

    public AdhesionDto fromEntityToDto(AdhesionEntity adhesionEntity);

}
