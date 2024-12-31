package org.mosqueethonon.v1.mapper.inscription;

import org.mapstruct.*;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.v1.dto.inscription.EleveDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EleveMapper {

    /**
     * Mapper utilisés essentiellement dans la fonctionnalité des inscriptions
     * On ne conserve pas les ids des élèves lors des conversions (pas utiles)
     */

    @Named("fromDtoToEntityIgnoreId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resultat", ignore = true)
    public EleveEntity fromDtoToEntityIgnoreId(EleveDto eleveDto) ;

    @Named("fromEntityToDtoIgnoreId")
    @InheritInverseConfiguration(name = "fromDtoToEntityIgnoreId")
    public EleveDto fromEntityToDtoIgnoreId(EleveEntity eleveEntity);


    /**
     * Mappers utilisés dans la gestion des classes, ou ici on conserve les ids des élèves
     */

    @Named("fromDtoToEntity")
    @Mapping(target = "resultat", ignore = true)
    public EleveEntity fromDtoToEntity(EleveDto eleveDto) ;

    @Named("fromEntityToDto")
    @InheritInverseConfiguration(name = "fromDtoToEntity")
    public EleveDto fromEntityToDto(EleveEntity eleveEntity);

    default void updateEleves(List<EleveDto> dtos, List<EleveEntity> entities) {
        if (dtos == null) {
            entities.clear();
            return;
        }

        Map<Long, EleveEntity> existingEntitiesMap = entities.stream()
                .filter(e -> e.getId() != null)
                .collect(Collectors.toMap(EleveEntity::getId, e -> e));

        List<EleveEntity> updatedEntities = new ArrayList<>();
        for (EleveDto dto : dtos) {
            EleveEntity entity;
            if (dto.getId() != null && existingEntitiesMap.containsKey(dto.getId())) {
                entity = existingEntitiesMap.get(dto.getId());
                this.updateEleve(dto, entity);
            } else {
                entity = this.fromDtoToEntity(dto);
            }
            updatedEntities.add(entity);
        }

        entities.clear();
        entities.addAll(updatedEntities);
    }

    @Mapping(target = "resultat", ignore = true)
    void updateEleve(EleveDto dto, @MappingTarget EleveEntity entity);

}
