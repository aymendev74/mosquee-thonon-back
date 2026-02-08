package org.mosqueethonon.v1.mapper.classe;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mosqueethonon.entity.classe.LienClasseEleveEntity;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.EleveRepository;
import org.mosqueethonon.v1.dto.classe.LienClasseEleveDto;
import org.mosqueethonon.v1.mapper.inscription.EleveMapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = { EleveMapper.class })
public abstract class LienClasseEleveMapper {

    @Autowired
    protected EleveRepository eleveRepository;

    @Autowired
    protected EleveMapper eleveMapper;

    public LienClasseEleveEntity fromDtoToEntity(LienClasseEleveDto lienClasseEleveDto) {
        if (lienClasseEleveDto == null) {
            return null;
        }
        LienClasseEleveEntity entity = new LienClasseEleveEntity();
        // Charger l'élève depuis la base de données pour avoir la version et tous les champs
        if (lienClasseEleveDto.getEleve() != null && lienClasseEleveDto.getEleve().getId() != null) {
            EleveEntity eleve = eleveRepository.findById(lienClasseEleveDto.getEleve().getId())
                .orElseThrow(() -> new ResourceNotFoundException("L'élève n'a pas été trouvé, id = " + lienClasseEleveDto.getEleve().getId()));
            entity.setEleve(eleve);
        }
        return entity;
    }

    @InheritInverseConfiguration
    @Mapping(target = "eleve", source = "eleve", qualifiedByName = "fromEntityToDto")
    public abstract LienClasseEleveDto fromEntityToDto(LienClasseEleveEntity lienClasseEleveEntity);

}
