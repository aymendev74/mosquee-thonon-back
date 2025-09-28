package org.mosqueethonon.v1.mapper.bulletin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.entity.bulletin.BulletinMatiereEntity;
import org.mosqueethonon.v1.dto.bulletin.BulletinDto;
import org.mosqueethonon.v1.dto.bulletin.BulletinMatiereDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BulletinMapper {

    BulletinDto fromEntityToDto(BulletinEntity bulletinEntity);

    @Mapping(target = "bulletinMatieres", ignore = true)
    BulletinEntity fromDtoToEntity(BulletinDto bulletinDto);

    @Mapping(target = "bulletinMatieres", ignore = true)
    void updateBulletinEntity(BulletinDto bulletinDto, @MappingTarget BulletinEntity bulletinEntity);

    default List<BulletinMatiereDto> mapBulletinMatiereEntitiesToBulletinMatiereDto(List<BulletinMatiereEntity> bulletinMatieres) {
        if (bulletinMatieres == null) return null;
        return bulletinMatieres.stream()
                .map(bulletinMatiere -> BulletinMatiereDto.builder()
                        .code(bulletinMatiere.getMatiere().getCode())
                        .remarque(bulletinMatiere.getRemarque())
                        .note(bulletinMatiere.getNote()).build())
                .toList();
    }
}
