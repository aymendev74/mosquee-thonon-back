package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.InscriptionEntity;
import org.mosqueethonon.v1.dto.InscriptionDto;

@Mapper(componentModel = "spring", uses = { SignatureMapper.class })
public interface InscriptionMapper {

    @Mapping(source = "dateNaissance", target = "dateNaissance", dateFormat = "dd.MM.yyyy")
    public InscriptionEntity fromDtoToEntity(InscriptionDto inscriptionDto) ;
    @Mapping(source = "dateNaissance", target = "dateNaissance", dateFormat = "dd.MM.yyyy")
    public InscriptionDto fromEntityToDto(InscriptionEntity inscriptionEntity);

}
