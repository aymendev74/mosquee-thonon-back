package org.mosqueethonon.classe.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.classe.entity.ClasseEntity;
import org.mosqueethonon.classe.entity.ClasseFeuillePresenceEntity;
import org.mosqueethonon.classe.entity.EleveFeuillePresenceEntity;
import org.mosqueethonon.classe.entity.FeuillePresenceEntity;
import org.mosqueethonon.common.exception.ResourceNotFoundException;
import org.mosqueethonon.classe.repository.ClasseFeuillePresenceRepository;
import org.mosqueethonon.classe.repository.ClasseRepository;
import org.mosqueethonon.classe.repository.FeuillePresenceRepository;
import org.mosqueethonon.classe.service.IFeuillePresenceService;
import org.mosqueethonon.classe.v1.dto.FeuillePresenceDto;
import org.mosqueethonon.classe.v1.dto.PresenceEleveDto;
import org.mosqueethonon.classe.v1.mapper.FeuillePresenceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class FeuillePresenceServiceImpl implements IFeuillePresenceService {

    private ClasseFeuillePresenceRepository classeFeuillePresenceRepository;
    private ClasseRepository classeRepository;
    private FeuillePresenceRepository feuillePresenceRepository;
    private FeuillePresenceMapper feuillePresenceMapper;

    @Override
    public FeuillePresenceDto createFeuillePresence(Long idClasse, FeuillePresenceDto feuillePresence) {
        this.classeRepository.findById(idClasse).orElseThrow(
                () -> new ResourceNotFoundException("La classe n'existe pas - idClas = " + idClasse)
        );
        ClasseFeuillePresenceEntity classeFeuillePresenceEntity = ClasseFeuillePresenceEntity.builder().idClasse(idClasse)
                .feuillePresence(this.mapFeuillePresenceDtoToEntity(feuillePresence)).build();
        classeFeuillePresenceEntity = this.classeFeuillePresenceRepository.save(classeFeuillePresenceEntity);
        return this.feuillePresenceMapper.fromEntityToDto(classeFeuillePresenceEntity);
    }

    private FeuillePresenceEntity mapFeuillePresenceDtoToEntity(FeuillePresenceDto feuillePresence) {
        return FeuillePresenceEntity.builder().date(feuillePresence.getDate())
                .elevesFeuillesPresences(this.mapElevesFeuillesPresencesDtoToEntity(feuillePresence.getPresenceEleves()))
                .build();
    }

    private List<EleveFeuillePresenceEntity> mapElevesFeuillesPresencesDtoToEntity(List<PresenceEleveDto> elevesFeuillesPresences) {
        return elevesFeuillesPresences.stream().map(presenceEleveDto -> EleveFeuillePresenceEntity.builder().idEleve(presenceEleveDto.getIdEleve())
                .present(presenceEleveDto.isPresent()).build()).toList();
    }

    @Override
    public List<FeuillePresenceDto> findFeuillePresencesByClasseId(Long idClasse) {
        ClasseEntity classe = this.classeRepository.findById(idClasse).orElseThrow(
                () -> new ResourceNotFoundException("La classe n'existe pas - idClas = " + idClasse)
        );
        return classe.getFeuillesPresences().stream().map(this.feuillePresenceMapper::fromEntityToDto)
                .sorted(Comparator.comparing(FeuillePresenceDto::getDate)).toList();
    }

    @Override
    public FeuillePresenceDto updateFeuillePresence(Long id, FeuillePresenceDto feuillePresence) {
        FeuillePresenceEntity feuillePresenceEntity = this.feuillePresenceRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("La feuille de presence n'existe pas - id = " + id)
        );
        this.feuillePresenceMapper.updateFeuillePresence(feuillePresence, feuillePresenceEntity);
        this.feuillePresenceRepository.save(feuillePresenceEntity);
        return this.feuillePresenceMapper.fromEntityToDto(feuillePresenceEntity);
    }

    @Override
    @Transactional
    public void deleteFeuillePresence(Long id) {
        FeuillePresenceEntity feuillePresenceEntity = this.feuillePresenceRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("La feuille de presence n'existe pas - id = " + id)
        );
        // Avant de supprimer la feuille, il faut supprimer le lien avec la classe
        ClasseFeuillePresenceEntity classeFeuillePresenceEntity = this.classeFeuillePresenceRepository.findByFeuillePresenceId(id);
        this.classeFeuillePresenceRepository.delete(classeFeuillePresenceEntity);
        this.feuillePresenceRepository.delete(feuillePresenceEntity);
    }

}
