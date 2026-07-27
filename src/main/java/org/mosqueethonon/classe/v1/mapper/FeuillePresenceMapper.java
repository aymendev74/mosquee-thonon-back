package org.mosqueethonon.classe.v1.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.classe.entity.ClasseFeuillePresenceEntity;
import org.mosqueethonon.classe.entity.EleveFeuillePresenceEntity;
import org.mosqueethonon.classe.entity.FeuillePresenceEntity;
import org.mosqueethonon.classe.v1.dto.FeuillePresenceDto;
import org.mosqueethonon.classe.v1.dto.PresenceEleveDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeuillePresenceMapper {

    @Mapping(source = "feuillePresence.date", target = "date")
    @Mapping(source = "feuillePresence.elevesFeuillesPresences", target = "presenceEleves")
    FeuillePresenceDto fromEntityToDto(ClasseFeuillePresenceEntity classeFeuillePresence);

    @Mapping(source = "idEleve", target = "idEleve")
    @Mapping(source = "present", target = "present")
    PresenceEleveDto toPresenceEleveDto(EleveFeuillePresenceEntity eleveFeuillePresence);

    List<PresenceEleveDto> toPresenceEleveDtoList(List<EleveFeuillePresenceEntity> eleveFeuillePresences);

    @InheritInverseConfiguration
    @Mapping(target = "id", ignore = true)
    ClasseFeuillePresenceEntity toEntity(FeuillePresenceDto dto);

    @Mapping(source = "presenceEleves", target = "elevesFeuillesPresences")
    @Mapping(target = "id", ignore = true)
    void updateFeuillePresence(FeuillePresenceDto dto, @MappingTarget FeuillePresenceEntity entity);

    @Mapping(source = "elevesFeuillesPresences", target = "presenceEleves")
    FeuillePresenceDto fromEntityToDto(FeuillePresenceEntity entity);

}