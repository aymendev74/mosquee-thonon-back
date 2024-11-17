package org.mosqueethonon.v1.mapper.classe;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.entity.classe.ClasseEntity;
import org.mosqueethonon.v1.dto.classe.ClasseDto;

@Mapper(componentModel = "spring", uses = { LienClasseEleveMapper.class })
public interface ClasseMapper {

    @Mapping(target = "id", ignore = true)
    ClasseEntity fromDtoToEntity(ClasseDto classeDto) ;

    @InheritInverseConfiguration(name = "fromDtoToEntity")
    ClasseDto fromEntityToDto(ClasseEntity classeEntity);

    @Mapping(target = "id", ignore = true)
    void updateClasseEntity(ClasseDto classeDto, @MappingTarget ClasseEntity classeEntity);

}
