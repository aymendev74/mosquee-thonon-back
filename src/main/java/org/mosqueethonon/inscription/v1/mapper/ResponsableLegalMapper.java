package org.mosqueethonon.inscription.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.inscription.entity.ResponsableLegalEntity;
import org.mosqueethonon.inscription.v1.dto.ResponsableLegalDto;

@Mapper(componentModel = "spring")
public interface ResponsableLegalMapper {

    public ResponsableLegalEntity fromDtoToEntity(ResponsableLegalDto responsableLegaldto) ;
    public ResponsableLegalDto fromEntityToDto(ResponsableLegalEntity responsableLegalEntity);

    void updateEntityFromDto(ResponsableLegalDto dto, @MappingTarget ResponsableLegalEntity entity);

}
