package org.mosqueethonon.referentiel.v1.mapper;

import org.mapstruct.Mapper;
import org.mosqueethonon.referentiel.entity.MatiereEntity;
import org.mosqueethonon.referentiel.v1.dto.MatiereDto;

@Mapper(componentModel = "spring")
public interface MatiereMapper {

    MatiereDto fromEntityToDto(MatiereEntity matiere);

}
