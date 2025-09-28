package org.mosqueethonon.v1.mapper.bulletin;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.bulletin.BulletinMatiereEntity;
import org.mosqueethonon.v1.dto.bulletin.BulletinMatiereDto;

@Mapper(componentModel = "spring")
public interface BulletinMatiereMapper {

    @Mapping(target = "matiere", ignore = true)
    BulletinMatiereEntity fromDtoToEntity(BulletinMatiereDto bulletinMatiereDto);


}
