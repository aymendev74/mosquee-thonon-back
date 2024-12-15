package org.mosqueethonon.service.impl.classe;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.classe.ClasseEntity;
import org.mosqueethonon.entity.classe.ClasseFeuillePresenceEntity;
import org.mosqueethonon.entity.classe.EleveFeuillePresenceEntity;
import org.mosqueethonon.entity.classe.FeuillePresenceEntity;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.ClasseFeuillePresenceRepository;
import org.mosqueethonon.repository.ClasseRepository;
import org.mosqueethonon.repository.FeuillePresenceRepository;
import org.mosqueethonon.service.classe.IFeuillePresenceService;
import org.mosqueethonon.v1.dto.classe.FeuillePresenceDto;
import org.mosqueethonon.v1.dto.classe.PresenceEleveDto;
import org.mosqueethonon.v1.mapper.classe.FeuillePresenceMapper;
import org.springframework.stereotype.Service;

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
        return classe.getFeuillesPresences().stream().map(this.feuillePresenceMapper::fromEntityToDto).toList();
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
}
