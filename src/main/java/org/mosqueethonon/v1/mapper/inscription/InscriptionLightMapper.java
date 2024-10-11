package org.mosqueethonon.v1.mapper.inscription;

import org.mapstruct.Mapper;
import org.mosqueethonon.entity.inscription.InscriptionLightEntity;
import org.mosqueethonon.v1.dto.inscription.InscriptionLightDto;

@Mapper(componentModel = "spring")
public interface InscriptionLightMapper {

    public InscriptionLightEntity fromDtoToEntity(InscriptionLightDto inscriptionDto) ;

    public InscriptionLightDto fromEntityToDto(InscriptionLightEntity inscriptionEntity);

}
