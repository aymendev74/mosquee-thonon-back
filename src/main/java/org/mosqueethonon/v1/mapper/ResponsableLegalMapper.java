package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mosqueethonon.entity.ResponsableLegalEntity;
import org.mosqueethonon.v1.dto.ResponsableLegalDto;

@Mapper(componentModel = "spring", uses = { SignatureMapper.class })
public interface ResponsableLegalMapper {

    public ResponsableLegalEntity fromDtoToEntity(ResponsableLegalDto responsableLegaldto) ;
    public ResponsableLegalDto fromEntityToDto(ResponsableLegalEntity responsableLegalEntity);


}
