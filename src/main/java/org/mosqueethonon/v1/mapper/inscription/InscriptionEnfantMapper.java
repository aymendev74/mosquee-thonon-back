package org.mosqueethonon.v1.mapper.inscription;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mapstruct.*;
import org.mosqueethonon.entity.inscription.InscriptionEnfantEntity;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = { EleveMapper.class, ResponsableLegalMapper.class }, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
@AllArgsConstructor
@NoArgsConstructor
public abstract class InscriptionEnfantMapper {

    private EleveMapper eleveMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eleves", source = "eleves", qualifiedByName = "fromDtoToEntityIgnoreId")
    public abstract InscriptionEnfantEntity fromDtoToEntity(InscriptionEnfantDto inscriptionEnfantDto) ;

    @InheritInverseConfiguration(name = "fromDtoToEntity")
    @Mapping(target = "eleves", source = "eleves", qualifiedByName = "fromEntityToDtoIgnoreId")
    public abstract InscriptionEnfantDto fromEntityToDto(InscriptionEnfantEntity inscriptionEnfantEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eleves", ignore = true)
    abstract void partialUpdateInscriptionEntity(InscriptionEnfantDto inscriptionEnfantDto, @MappingTarget InscriptionEnfantEntity inscriptionEnfantEntity);

    public void updateInscriptionEntity(InscriptionEnfantDto inscriptionEnfantDto, @MappingTarget InscriptionEnfantEntity inscriptionEnfantEntity) {
        this.partialUpdateInscriptionEntity(inscriptionEnfantDto, inscriptionEnfantEntity);
        this.eleveMapper.updateEleves(inscriptionEnfantDto.getEleves(), inscriptionEnfantEntity.getEleves());
    }

    // Important - for testing purposes
    @Autowired
    public void setEleveMapper(EleveMapper eleveMapper) {
        this.eleveMapper = eleveMapper;
    }
}
