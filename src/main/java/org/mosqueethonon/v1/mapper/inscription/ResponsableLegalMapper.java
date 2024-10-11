package org.mosqueethonon.v1.mapper.inscription;

import org.mapstruct.Mapper;
import org.mosqueethonon.entity.inscription.ResponsableLegalEntity;
import org.mosqueethonon.v1.dto.inscription.ResponsableLegalDto;

@Mapper(componentModel = "spring")
public interface ResponsableLegalMapper {

    public ResponsableLegalEntity fromDtoToEntity(ResponsableLegalDto responsableLegaldto) ;
    public ResponsableLegalDto fromEntityToDto(ResponsableLegalEntity responsableLegalEntity);


}
