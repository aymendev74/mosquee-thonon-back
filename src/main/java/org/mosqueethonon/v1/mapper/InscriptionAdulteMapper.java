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
    @Mapping(source = "idTarif", target = "responsableLegal.idTarif")
    @Mapping(source = "idResponsableLegal", target = "responsableLegal.id")
    public abstract InscriptionAdulteEntity fromDtoToEntity(InscriptionAdulteDto inscriptionAdulteDto) ;

    @InheritInverseConfiguration
    public abstract InscriptionAdulteDto fromEntityToDto(InscriptionAdulteEntity inscriptionAdulteEntity);

    @AfterMapping
    protected void mapEleveEntityToDto(InscriptionAdulteEntity entity, @MappingTarget InscriptionAdulteDto dto) {
        if(CollectionUtils.isNotEmpty(entity.getEleves())) {
            EleveEntity eleve = entity.getEleves().get(0);
            dto.setDateNaissance(eleve.getDateNaissance());
            dto.setSexe(eleve.getSexe());
            dto.setNiveauInterne(eleve.getNiveauInterne());
            dto.setSignatureEleve(signatureMapper.fromEntityToDto(eleve.getSignature()));
            dto.setIdEleve(eleve.getId());
            dto.setIdTarif(eleve.getIdTarif());
            dto.setSignatureResponsableLegal(signatureMapper.fromEntityToDto(entity.getResponsableLegal().getSignature()));
        }
    }

    @AfterMapping
    protected void mapDtoToEleveEntity(InscriptionAdulteDto dto, @MappingTarget InscriptionAdulteEntity entity) {
        EleveEntity eleveEntity = new EleveEntity();
        eleveEntity.setId(dto.getIdEleve());
        eleveEntity.setPrenom(dto.getPrenom());
        eleveEntity.setNom(dto.getNom());
        eleveEntity.setDateNaissance(dto.getDateNaissance());
        eleveEntity.setSignature(signatureMapper.fromDtoToEntity(dto.getSignatureEleve()));
        eleveEntity.setSexe(dto.getSexe());
        eleveEntity.setNiveauInterne(dto.getNiveauInterne());
        eleveEntity.setIdTarif(dto.getIdTarif());
        entity.getResponsableLegal().setSignature(signatureMapper.fromDtoToEntity(dto.getSignatureResponsableLegal()));
        entity.setEleves(new ArrayList<>());
        entity.getEleves().add(eleveEntity);
    }
}
