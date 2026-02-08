package org.mosqueethonon.v1.mapper.inscription;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.entity.inscription.InscriptionEntity;
import org.mosqueethonon.v1.dto.inscription.EleveAvecAutorisationsDto;

@Mapper(componentModel = "spring", uses = {EleveMapper.class})
public interface EleveAvecAutorisationsMapper {

    @Mapping(source = "eleve.id", target = "id")
    @Mapping(source = "eleve.nom", target = "nom")
    @Mapping(source = "eleve.prenom", target = "prenom")
    @Mapping(source = "eleve.dateNaissance", target = "dateNaissance")
    @Mapping(source = "eleve.niveau", target = "niveau")
    @Mapping(source = "eleve.niveauInterne", target = "niveauInterne")
    @Mapping(source = "eleve.resultat", target = "resultat")
    @Mapping(source = "eleve.classeId", target = "classeId")
    @Mapping(source = "inscription.autorisationAutonomie", target = "autorisationAutonomie")
    @Mapping(source = "inscription.autorisationMedia", target = "autorisationMedia")
    EleveAvecAutorisationsDto toEleveAvecAutorisationsDto(EleveEntity eleve, InscriptionEntity inscription);

}
