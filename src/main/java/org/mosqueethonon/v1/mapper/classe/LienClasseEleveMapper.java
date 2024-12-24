package org.mosqueethonon.v1.mapper.classe;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.classe.LienClasseEleveEntity;
import org.mosqueethonon.v1.dto.classe.LienClasseEleveDto;
import org.mosqueethonon.v1.mapper.inscription.EleveMapper;

@Mapper(componentModel = "spring", uses = { EleveMapper.class })
public interface LienClasseEleveMapper {

    @Mapping(target = "eleve", source = "eleve", qualifiedByName = "fromDtoToEntity")
    public LienClasseEleveEntity fromDtoToEntity(LienClasseEleveDto lienClasseEleveDto);

    @InheritInverseConfiguration
    @Mapping(target = "eleve", source = "eleve", qualifiedByName = "fromEntityToDto")
    public LienClasseEleveDto fromEntityToDto(LienClasseEleveEntity lienClasseEleveEntity);

}
