package org.mosqueethonon.utilisateur.v1.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mosqueethonon.utilisateur.entity.UtilisateurRoleEntity;
import org.mosqueethonon.utilisateur.v1.dto.RoleDto;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleDto fromEntityToDto(UtilisateurRoleEntity role);

    @InheritInverseConfiguration
    UtilisateurRoleEntity fromDtoToEntity(RoleDto role);

}
