package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.InscriptionEntity;
import org.mosqueethonon.v1.dto.InscriptionDto;

@Mapper(componentModel = "spring", uses = { SignatureMapper.class,
        EleveMapper.class, ResponsableLegalMapper.class })
public interface InscriptionMapper {

    public InscriptionEntity fromDtoToEntity(InscriptionDto inscriptionDto) ;

    public InscriptionDto fromEntityToDto(InscriptionEntity inscriptionEntity);

}
