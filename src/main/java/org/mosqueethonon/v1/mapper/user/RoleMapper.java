package org.mosqueethonon.v1.mapper.user;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mosqueethonon.entity.utilisateur.UtilisateurRoleEntity;
import org.mosqueethonon.v1.dto.user.RoleDto;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleDto fromEntityToDto(UtilisateurRoleEntity role);

    @InheritInverseConfiguration
    UtilisateurRoleEntity fromDtoToEntity(RoleDto role);

}
