package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mosqueethonon.entity.InscriptionEnfantEntity;
import org.mosqueethonon.v1.dto.InscriptionDto;

@Mapper(componentModel = "spring", uses = { SignatureMapper.class,
        EleveMapper.class, ResponsableLegalMapper.class })
public interface InscriptionMapper {

    public InscriptionEnfantEntity fromDtoToEntity(InscriptionDto inscriptionDto) ;

    public InscriptionDto fromEntityToDto(InscriptionEnfantEntity inscriptionEnfantEntity);

}
