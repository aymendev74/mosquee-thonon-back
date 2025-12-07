package org.mosqueethonon.v1.mapper.classe;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mosqueethonon.entity.classe.ClasseEntity;
import org.mosqueethonon.v1.dto.classe.ClasseDto;

@Mapper(componentModel = "spring", uses = { LienClasseEleveMapper.class })
public interface ClasseMapper {

    @Mapping(target = "id", ignore = true)
    ClasseEntity fromDtoToEntity(ClasseDto classeDto) ;

    default String concatNomPrenomEnseignant(ClasseEntity classeEntity) {
        if (classeEntity.getEnseignant() == null) {
            return null;
        }
        String prenom = classeEntity.getEnseignant().getPrenom();
        String nom = classeEntity.getEnseignant().getNom();
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

    @InheritInverseConfiguration(name = "fromDtoToEntity")
    @Mapping(target = "nomPrenomEnseignant", expression = "java(concatNomPrenomEnseignant(classeEntity))")
    ClasseDto fromEntityToDto(ClasseEntity classeEntity);

    @Mapping(target = "id", ignore = true)
    void updateClasseEntity(ClasseDto classeDto, @MappingTarget ClasseEntity classeEntity);

}
