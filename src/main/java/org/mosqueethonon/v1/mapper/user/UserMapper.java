package org.mosqueethonon.v1.mapper.user;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.v1.dto.user.UserDto;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public interface UserMapper {

    UserDto fromEntityToDto(UtilisateurEntity user);

    @InheritInverseConfiguration
    UtilisateurEntity fromDtoToEntity(UserDto user);

}
