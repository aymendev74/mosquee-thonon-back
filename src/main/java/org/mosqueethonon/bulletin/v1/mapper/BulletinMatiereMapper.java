package org.mosqueethonon.bulletin.v1.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.bulletin.entity.BulletinMatiereEntity;
import org.mosqueethonon.bulletin.v1.dto.BulletinMatiereDto;

@Mapper(componentModel = "spring")
public interface BulletinMatiereMapper {

    @Mapping(target = "matiere", ignore = true)
    BulletinMatiereEntity fromDtoToEntity(BulletinMatiereDto bulletinMatiereDto);


}
