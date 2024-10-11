package org.mosqueethonon.v1.mapper.inscription;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;

@Mapper(componentModel = "spring", uses = { EleveMapper.class, ResponsableLegalMapper.class })
public interface InscriptionEnfantMapper {

    public InscriptionEnfantEntity fromDtoToEntity(InscriptionEnfantDto inscriptionEnfantDto) ;

    @InheritInverseConfiguration(name = "fromDtoToEntity")
    public InscriptionEnfantDto fromEntityToDto(InscriptionEnfantEntity inscriptionEnfantEntity);

    public void updateInscriptionEntity(InscriptionEnfantDto inscriptionEnfantDto, @MappingTarget InscriptionEnfantEntity inscriptionEnfantEntity);
}
