package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.Signature;
import org.mosqueethonon.v1.dto.SignatureDto;

@Mapper(componentModel = "spring")
public interface SignatureMapper {

    public Signature fromDtoToEntity(SignatureDto signature) ;

    public SignatureDto fromEntityToDto(Signature signature);


}
