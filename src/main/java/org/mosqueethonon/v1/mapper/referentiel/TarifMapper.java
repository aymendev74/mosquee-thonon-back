package org.mosqueethonon.v1.mapper.referentiel;

import org.mapstruct.Mapper;
import org.mosqueethonon.entity.referentiel.TarifEntity;
import org.mosqueethonon.v1.dto.referentiel.TarifDto;

@Mapper(componentModel = "spring")
public interface TarifMapper {

    public TarifEntity fromDtoToEntity(TarifDto tarif) ;

    public TarifDto fromEntityToDto(TarifEntity tarif);


}
