package org.mosqueethonon.v1.mapper.classe;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.entity.classe.ClasseFeuillePresenceEntity;
import org.mosqueethonon.entity.classe.EleveFeuillePresenceEntity;
import org.mosqueethonon.entity.classe.FeuillePresenceEntity;
import org.mosqueethonon.v1.dto.classe.FeuillePresenceDto;
import org.mosqueethonon.v1.dto.classe.PresenceEleveDto;

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