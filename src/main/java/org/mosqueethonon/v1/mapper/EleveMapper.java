package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.EleveEntity;
import org.mosqueethonon.v1.dto.EleveDto;

@Mapper(componentModel = "spring", uses = { SignatureMapper.class })
public interface EleveMapper {

    public EleveEntity fromDtoToEntity(EleveDto eleveDto) ;

    public EleveDto fromEntityToDto(EleveEntity eleveEntity);

}
