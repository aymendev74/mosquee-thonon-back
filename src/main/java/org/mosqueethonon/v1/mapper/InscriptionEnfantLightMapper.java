package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mosqueethonon.entity.InscriptionLightEntity;
import org.mosqueethonon.v1.dto.InscriptionEnfantLightDto;

@Mapper(componentModel = "spring")
public interface InscriptionEnfantLightMapper {

    public InscriptionLightEntity fromDtoToEntity(InscriptionEnfantLightDto inscriptionDto) ;

    public InscriptionEnfantLightDto fromEntityToDto(InscriptionLightEntity inscriptionEntity);

}
