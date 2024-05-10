package org.mosqueethonon.v1.mapper;

import org.mapstruct.Mapper;
import org.mosqueethonon.entity.TarifEntity;
import org.mosqueethonon.v1.dto.TarifDto;

@Mapper(componentModel = "spring", uses = { PeriodeInfoMapper.class })
public interface TarifMapper {

    public TarifEntity fromDtoToEntity(TarifDto tarif) ;

    public TarifDto fromEntityToDto(TarifEntity tarif);


}
