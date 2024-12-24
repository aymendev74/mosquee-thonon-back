package org.mosqueethonon.v1.mapper.inscription;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;

@Mapper(componentModel = "spring", uses = { EleveMapper.class, ResponsableLegalMapper.class })
public interface InscriptionEnfantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eleves", source = "eleves", qualifiedByName = "fromDtoToEntityIgnoreId")
    public InscriptionEnfantEntity fromDtoToEntity(InscriptionEnfantDto inscriptionEnfantDto) ;

    @InheritInverseConfiguration(name = "fromDtoToEntity")
    @Mapping(target = "eleves", source = "eleves", qualifiedByName = "fromEntityToDtoIgnoreId")
    public InscriptionEnfantDto fromEntityToDto(InscriptionEnfantEntity inscriptionEnfantEntity);

    @Mapping(target = "eleves", source = "eleves", qualifiedByName = "fromDtoToEntityIgnoreId")
    @Mapping(target = "id", ignore = true)
    public void updateInscriptionEntity(InscriptionEnfantDto inscriptionEnfantDto, @MappingTarget InscriptionEnfantEntity inscriptionEnfantEntity);

}
