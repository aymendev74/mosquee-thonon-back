package org.mosqueethonon.v1.mapper.classe;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.classe.ClasseFeuillePresenceEntity;
import org.mosqueethonon.entity.classe.EleveFeuillePresenceEntity;
import org.mosqueethonon.v1.dto.classe.FeuillePresenceDto;
import org.mosqueethonon.v1.dto.classe.PresenceEleveDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeuillePresenceMapper {

    // Mapper principal : FeuillePresenceEntity -> FeuillePresenceDto
    @Mapping(source = "feuillePresence.date", target = "date")
    @Mapping(source = "feuillePresence.elevesFeuillesPresences", target = "presenceEleves")
    FeuillePresenceDto fromEntityToDto(ClasseFeuillePresenceEntity classeFeuillePresence);

    // Mapper pour les sous-objets : EleveFeuillePresenceEntity -> PresenceEleveDto
    @Mapping(source = "idEleve", target = "idEleve")
    @Mapping(source = "present", target = "present")
    PresenceEleveDto toPresenceEleveDto(EleveFeuillePresenceEntity eleveFeuillePresence);

    // Mapper pour une liste d'EleveFeuillePresenceEntity
    List<PresenceEleveDto> toPresenceEleveDtoList(List<EleveFeuillePresenceEntity> eleveFeuillePresences);

    // Méthode inverse si nécessaire (DTO -> Entité)
    @InheritInverseConfiguration
    ClasseFeuillePresenceEntity toEntity(FeuillePresenceDto dto);

}