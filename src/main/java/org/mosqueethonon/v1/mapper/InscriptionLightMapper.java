package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.InscriptionLightEntity;
import org.mosqueethonon.v1.dto.InscriptionLightDto;

@Mapper(componentModel = "spring")
public interface InscriptionLightMapper {

    public InscriptionLightEntity fromDtoToEntity(InscriptionLightDto inscriptionDto) ;

    public InscriptionLightDto fromEntityToDto(InscriptionLightEntity inscriptionEntity);

}
