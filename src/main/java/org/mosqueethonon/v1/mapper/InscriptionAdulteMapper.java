package org.mosqueethonon.v1.mapper;

import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.*;
import org.mosqueethonon.entity.EleveEntity;
import org.mosqueethonon.entity.InscriptionAdulteEntity;
import org.mosqueethonon.v1.dto.InscriptionAdulteDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

@Mapper(componentModel = "spring")
public abstract class InscriptionAdulteMapper {

    @Autowired
    protected SignatureMapper signatureMapper;

    @Mapping(source = "nom", target = "responsableLegal.nom")
    @Mapping(source = "prenom", target = "responsableLegal.prenom")
    @Mapping(source = "email", target = "responsableLegal.email")
    @Mapping(source = "mobile", target = "responsableLegal.mobile")
    @Mapping(source = "numeroEtRue", target = "responsableLegal.numeroEtRue")
    @Mapping(source = "codePostal", target = "responsableLegal.codePostal")
    @Mapping(source = "ville", target = "responsableLegal.ville")
    public abstract InscriptionAdulteEntity fromDtoToEntity(InscriptionAdulteDto inscriptionAdulteDto);

    @InheritInverseConfiguration(name = "fromDtoToEntity")
    public abstract InscriptionAdulteDto fromEntityToDto(InscriptionAdulteEntity inscriptionAdulteEntity);

    @Mapping(source = "nom", target = "responsableLegal.nom")
    @Mapping(source = "prenom", target = "responsableLegal.prenom")
    @Mapping(source = "email", target = "responsableLegal.email")
    @Mapping(source = "mobile", target = "responsableLegal.mobile")
    @Mapping(source = "numeroEtRue", target = "responsableLegal.numeroEtRue")
    @Mapping(source = "codePostal", target = "responsableLegal.codePostal")
    @Mapping(source = "ville", target = "responsableLegal.ville")
    public abstract void updateInscriptionEntity(InscriptionAdulteDto dto, @MappingTarget InscriptionAdulteEntity entity);

    @AfterMapping
    protected void mapEleveEntityToDto(InscriptionAdulteEntity entity, @MappingTarget InscriptionAdulteDto dto) {
        if (CollectionUtils.isNotEmpty(entity.getEleves())) {
            EleveEntity eleve = entity.getEleves().get(0);
            dto.setDateNaissance(eleve.getDateNaissance());
            dto.setSexe(eleve.getSexe());
            dto.setNiveauInterne(eleve.getNiveauInterne());
        }
    }

    @AfterMapping
    protected void mapDtoToEleveEntity(InscriptionAdulteDto dto, @MappingTarget InscriptionAdulteEntity entity) {
        // Il faut distinguer le cas ou on est en update et le cas ou on est en création
        EleveEntity eleveEntity = null;
        if (CollectionUtils.isEmpty(entity.getEleves())) {
            entity.setEleves(new ArrayList<>());
            eleveEntity = new EleveEntity();
            entity.getEleves().add(eleveEntity);
        } else {
            eleveEntity = entity.getEleves().get(0);
        }
        eleveEntity.setPrenom(dto.getPrenom());
        eleveEntity.setNom(dto.getNom());
        eleveEntity.setDateNaissance(dto.getDateNaissance());
        eleveEntity.setSexe(dto.getSexe());
        eleveEntity.setNiveauInterne(dto.getNiveauInterne());
    }
}
