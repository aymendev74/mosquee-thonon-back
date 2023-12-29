package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.AdhesionEntity;
import org.mosqueethonon.v1.dto.AdhesionDto;

@Mapper(componentModel = "spring", uses = { SignatureMapper.class })
public interface AdhesionMapper {

    @Mapping(source = "dateNaissance", target = "dateNaissance", dateFormat = "dd.MM.yyyy")
    public AdhesionEntity fromDtoToEntity(AdhesionDto adhesionDto) ;

    @Mapping(source = "dateNaissance", target = "dateNaissance", dateFormat = "dd.MM.yyyy")
    public AdhesionDto fromEntityToDto(AdhesionEntity adhesionEntity);

}
