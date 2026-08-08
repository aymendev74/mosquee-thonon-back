package org.mosqueethonon.paiement.v1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.paiement.entity.PaiementEntity;
import org.mosqueethonon.paiement.v1.dto.PaiementDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaiementMapper {

    /**
     * L'identifiant, le statut et les colonnes techniques ne sont jamais pilotés par le client : le
     * statut est posé par le service, la signature par le listener d'audit.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "signature", ignore = true)
    @Mapping(target = "version", ignore = true)
    PaiementEntity fromDtoToEntity(PaiementDto dto);

    PaiementDto fromEntityToDto(PaiementEntity entity);

    List<PaiementDto> fromEntitiesToDtos(List<PaiementEntity> entities);

}
