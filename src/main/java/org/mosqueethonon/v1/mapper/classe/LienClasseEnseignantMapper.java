package org.mosqueethonon.v1.mapper.classe;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.classe.LienClasseEnseignantEntity;
import org.mosqueethonon.v1.dto.classe.EnseignantDto;

@Mapper(componentModel = "spring")
public interface LienClasseEnseignantMapper {

    @Mapping(target = "id", source = "idUtilisateur")
    @Mapping(target = "nomPrenom", expression = "java(concatNomPrenomEnseignant(entity))")
    EnseignantDto fromEntityToDto(LienClasseEnseignantEntity entity);

    default String concatNomPrenomEnseignant(LienClasseEnseignantEntity entity) {
        if (entity.getEnseignant() == null) {
            return null;
        }
        String prenom = entity.getEnseignant().getPrenom();
        String nom = entity.getEnseignant().getNom();
        if (prenom == null && nom == null) {
            return null;
        }
        if (prenom == null) {
            return nom;
        }
        if (nom == null) {
            return prenom;
        }
        return prenom + " " + nom;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idClasse", ignore = true)
    @Mapping(target = "idUtilisateur", source = "id")
    @Mapping(target = "enseignant", ignore = true)
    @Mapping(target = "signature", ignore = true)
    LienClasseEnseignantEntity fromDtoToEntity(EnseignantDto dto);

}
