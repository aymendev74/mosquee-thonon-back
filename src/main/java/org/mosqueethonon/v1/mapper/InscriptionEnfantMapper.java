package org.mosqueethonon.v1.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.entity.InscriptionEnfantEntity;
import org.mosqueethonon.v1.dto.InscriptionEnfantDto;

@Mapper(componentModel = "spring", uses = { SignatureMapper.class,
        EleveMapper.class, ResponsableLegalMapper.class })
public interface InscriptionEnfantMapper {

    public InscriptionEnfantEntity fromDtoToEntity(InscriptionEnfantDto inscriptionEnfantDto) ;

    @InheritInverseConfiguration(name = "fromDtoToEntity")
    public InscriptionEnfantDto fromEntityToDto(InscriptionEnfantEntity inscriptionEnfantEntity);

    public void updateInscriptionEntity(InscriptionEnfantDto inscriptionEnfantDto, @MappingTarget InscriptionEnfantEntity inscriptionEnfantEntity);
}
