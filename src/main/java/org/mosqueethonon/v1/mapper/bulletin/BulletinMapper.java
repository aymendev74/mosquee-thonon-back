package org.mosqueethonon.v1.mapper.bulletin;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.v1.dto.bulletin.BulletinDto;

@Mapper(componentModel = "spring")
public interface BulletinMapper {

    BulletinDto fromEntityToDto(BulletinEntity bulletinEntity);

    BulletinEntity fromDtoToEntity(BulletinDto bulletinDto);

    void updateBulletinEntity(BulletinDto bulletinDto, @MappingTarget BulletinEntity bulletinEntity);

}
