package org.mosqueethonon.inscription.v1.mapper;

import org.mapstruct.Mapper;
import org.mosqueethonon.inscription.entity.InscriptionLightEntity;
import org.mosqueethonon.inscription.v1.dto.InscriptionLightDto;

@Mapper(componentModel = "spring")
public interface InscriptionLightMapper {

    public InscriptionLightEntity fromDtoToEntity(InscriptionLightDto inscriptionDto) ;

    public InscriptionLightDto fromEntityToDto(InscriptionLightEntity inscriptionEntity);

}
