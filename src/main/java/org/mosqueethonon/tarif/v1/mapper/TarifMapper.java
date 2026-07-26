package org.mosqueethonon.tarif.v1.mapper;

import org.mapstruct.Mapper;
import org.mosqueethonon.tarif.entity.TarifEntity;
import org.mosqueethonon.tarif.v1.dto.TarifDto;

@Mapper(componentModel = "spring")
public interface TarifMapper {

    public TarifEntity fromDtoToEntity(TarifDto tarif) ;

    public TarifDto fromEntityToDto(TarifEntity tarif);


}
