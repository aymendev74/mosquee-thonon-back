package org.mosqueethonon.service.impl.bulletin;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.entity.bulletin.BulletinMatiereEntity;
import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.BulletinRepository;
import org.mosqueethonon.service.bulletin.BulletinService;
import org.mosqueethonon.service.inscription.EleveService;
import org.mosqueethonon.service.referentiel.MatiereService;
import org.mosqueethonon.v1.dto.bulletin.BulletinDto;
import org.mosqueethonon.v1.dto.bulletin.BulletinMatiereDto;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.dto.referentiel.MatiereDto;
import org.mosqueethonon.v1.mapper.bulletin.BulletinMapper;
import org.mosqueethonon.v1.mapper.bulletin.BulletinMatiereMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class BulletinServiceImpl implements BulletinService {

    private EleveService eleveService;

    private BulletinRepository bulletinRepository;

    private BulletinMapper bulletinMapper;

    private BulletinMatiereMapper bulletinMatiereMapper;

    private MatiereService matiereService;

    @Override
    public List<BulletinDto> findBulletinsByIdEleve(Long idEleve) {
        EleveDto eleve = this.eleveService.findEleveById(idEleve);
        if (eleve == null) {
            throw new ResourceNotFoundException("Eleve inexistant ! id = " + idEleve);
        }
        List<BulletinEntity> bulletins = this.bulletinRepository.findByIdEleve(idEleve);
        return bulletins.stream().map(bulletinMapper::fromEntityToDto).collect(Collectors.toList());
    }

    @Override
    public BulletinDto createBulletin(BulletinDto bulletinDto) {
        BulletinEntity bulletinEntity = bulletinMapper.fromDtoToEntity(bulletinDto);
        bulletinEntity.setBulletinMatieres(this.mapBulletinMatieres(bulletinDto.getBulletinMatieres()));
        return bulletinMapper.fromEntityToDto(this.bulletinRepository.save(bulletinEntity));
    }

    private List<BulletinMatiereEntity> mapBulletinMatieres(List<BulletinMatiereDto> bulletinMatieres) {
        List<BulletinMatiereEntity> bulletinMatieresEntities = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(bulletinMatieres)) {
            for(BulletinMatiereDto bulletinMatiereDto : bulletinMatieres) {
                MatiereEntity matiere = this.matiereService.findByCode(bulletinMatiereDto.getCode()).orElseThrow(
                        () -> new ResourceNotFoundException("La matiere est inexistante ! code = " + bulletinMatiereDto.getCode()));
                BulletinMatiereEntity bulletinMatiereEntity = this.bulletinMatiereMapper.fromDtoToEntity(bulletinMatiereDto);
                bulletinMatiereEntity.setMatiere(matiere);
                bulletinMatieresEntities.add(bulletinMatiereEntity);
            }
        }
        return bulletinMatieresEntities;
    }

    @Override
    public BulletinDto updateBulletin(Long id, BulletinDto bulletinDto) {
        BulletinEntity bulletinEntity = this.bulletinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bulletin inexistant ! id = " + id));
        bulletinMapper.updateBulletinEntity(bulletinDto, bulletinEntity);
        bulletinEntity.getBulletinMatieres().clear();
        bulletinEntity.getBulletinMatieres().addAll(this.mapBulletinMatieres(bulletinDto.getBulletinMatieres()));
        return bulletinMapper.fromEntityToDto(this.bulletinRepository.save(bulletinEntity));
    }

    @Override
    public void deleteBulletin(Long id) {
        this.bulletinRepository.deleteById(id);
    }
}
