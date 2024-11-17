package org.mosqueethonon.v1.mapper.inscription;

import org.mapstruct.*;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.v1.dto.inscription.EleveDto;

@Mapper(componentModel = "spring")
public interface EleveMapper {

    /**
     * Mapper utilisés essentiellement dans la fonctionnalité des inscriptions
     * On ne conserve pas les ids des élèves lors des conversions (pas utiles)
     */

    @Named("fromDtoToEntityIgnoreId")
    @Mapping(target = "id", ignore = true)
    public EleveEntity fromDtoToEntityIgnoreId(EleveDto eleveDto) ;

    @Named("fromEntityToDtoIgnoreId")
    @InheritInverseConfiguration(name = "fromDtoToEntityIgnoreId")
    public EleveDto fromEntityToDtoIgnoreId(EleveEntity eleveEntity);


    /**
     * Mappers utilisés dans la gestion des classes, ou ici on conserve les ids des élèves
     */

    @Named("fromDtoToEntity")
    public EleveEntity fromDtoToEntity(EleveDto eleveDto) ;

    @Named("fromEntityToDto")
    @InheritInverseConfiguration(name = "fromDtoToEntity")
    public EleveDto fromEntityToDto(EleveEntity eleveEntity);

}
