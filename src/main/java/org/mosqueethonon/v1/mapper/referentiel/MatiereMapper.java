package org.mosqueethonon.v1.mapper.referentiel;

import org.mapstruct.Mapper;
import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.v1.dto.referentiel.MatiereDto;

@Mapper(componentModel = "spring")
public interface MatiereMapper {

    MatiereDto fromEntityToDto(MatiereEntity matiere);

}
