package org.mosqueethonon.utilisateur.v1.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.utilisateur.entity.UtilisateurEntity;
import org.mosqueethonon.utilisateur.v1.dto.UserDto;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    UserDto fromEntityToDto(UtilisateurEntity user);

    @InheritInverseConfiguration
    UtilisateurEntity fromDtoToEntity(UserDto user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    void updateUserEntityFromDto(UserDto user, @MappingTarget UtilisateurEntity utilisateurEntity);

}
