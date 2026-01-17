package org.mosqueethonon.v1.mapper.classe;

import org.mapstruct.*;
import org.mosqueethonon.entity.classe.ClasseEntity;
import org.mosqueethonon.entity.classe.LienClasseEnseignantEntity;
import org.mosqueethonon.v1.dto.classe.ClasseDto;

@Mapper(componentModel = "spring", uses = { LienClasseEleveMapper.class, LienClasseEnseignantMapper.class })
public interface ClasseMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "liensClasseEnseignants", source = "enseignants")
    @BeanMapping(builder = @Builder(disableBuilder = true)) // pour que afterMapping soit appelé (bug connu chez mapstruct)
    ClasseEntity fromDtoToEntity(ClasseDto classeDto);

    @InheritInverseConfiguration(name = "fromDtoToEntity")
    @Mapping(target = "enseignants", source = "liensClasseEnseignants")
    ClasseDto fromEntityToDto(ClasseEntity classeEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "liensClasseEnseignants", source = "enseignants")
    void updateClasseEntity(ClasseDto classeDto, @MappingTarget ClasseEntity classeEntity);

    @AfterMapping
    default void setIdClasseInLiensEnseignants(@MappingTarget ClasseEntity classeEntity) {
        if (classeEntity.getLiensClasseEnseignants() != null) {
            for (LienClasseEnseignantEntity lien : classeEntity.getLiensClasseEnseignants()) {
                lien.setIdClasse(classeEntity.getId());
            }
        }
    }

}
