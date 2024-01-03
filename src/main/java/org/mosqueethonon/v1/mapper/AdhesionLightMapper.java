package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.AdhesionLightEntity;
import org.mosqueethonon.v1.dto.AdhesionLightDto;

@Mapper(componentModel = "spring", uses = { SignatureMapper.class })
public interface AdhesionLightMapper {

    @Mapping(source = "dateInscription", target = "dateInscription", dateFormat = "dd.MM.yyyy")
    public AdhesionLightEntity fromDtoToEntity(AdhesionLightDto adhesionDto) ;

    @Mapping(source = "dateInscription", target = "dateInscription", dateFormat = "dd.MM.yyyy")
    public AdhesionLightDto fromEntityToDto(AdhesionLightEntity adhesionEntity);


}
