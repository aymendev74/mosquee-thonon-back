package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.InscriptionLightEntity;
import org.mosqueethonon.v1.dto.InscriptionLightDto;

@Mapper(componentModel = "spring")
public interface InscriptionLightMapper {


    @Mapping(source = "dateInscription", target = "dateInscription", dateFormat = "dd.MM.yyyy")
    @Mapping(source = "dateNaissance", target = "dateNaissance", dateFormat = "dd.MM.yyyy")
    public InscriptionLightEntity fromDtoToEntity(InscriptionLightDto inscriptionDto) ;

    @Mapping(source = "dateInscription", target = "dateInscription", dateFormat = "dd.MM.yyyy")
    @Mapping(source = "dateNaissance", target = "dateNaissance", dateFormat = "dd.MM.yyyy")
    public InscriptionLightDto fromEntityToDto(InscriptionLightEntity inscriptionEntity);


}
