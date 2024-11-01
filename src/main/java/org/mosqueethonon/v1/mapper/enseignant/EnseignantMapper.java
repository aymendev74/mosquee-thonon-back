package org.mosqueethonon.v1.mapper.enseignant;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.entity.classe.EnseignantEntity;
import org.mosqueethonon.v1.dto.enseignant.EnseignantDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EnseignantMapper {

    EnseignantDto fromEntityToDto(EnseignantEntity enseignantEntity);

    @InheritInverseConfiguration
    @Mapping(target = "id", ignore = true)
    EnseignantEntity fromDtoToEntity(EnseignantDto enseignantDto);

    List<EnseignantDto> fromEntityToDto(List<EnseignantEntity> enseignantEntities);

    @Mapping(target = "id", ignore = true)
    @InheritInverseConfiguration
    List<EnseignantEntity> fromDtoToEntity(List<EnseignantDto> enseignantDtos);

    @Mapping(target = "id", ignore = true)
    void updateEnseignantEntity(EnseignantDto enseignantDto, @MappingTarget EnseignantEntity enseignantEntity);

}
