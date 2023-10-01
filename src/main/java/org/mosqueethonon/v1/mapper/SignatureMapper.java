package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.Signature;
import org.mosqueethonon.v1.dto.SignatureDto;

@Mapper(componentModel = "spring")
public interface SignatureMapper {

    @Mapping(source = "dateCreation", target = "dateCreation", dateFormat = "dd.MM.yyyy")
    @Mapping(source = "dateModification", target = "dateModification", dateFormat = "dd.MM.yyyy")
    public Signature fromDtoToEntity(SignatureDto signature) ;

    @Mapping(source = "dateCreation", target = "dateCreation", dateFormat = "dd.MM.yyyy")
    @Mapping(source = "dateModification", target = "dateModification", dateFormat = "dd.MM.yyyy")
    public SignatureDto fromEntityToDto(Signature signature);


}
