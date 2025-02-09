package org.mosqueethonon.service.impl.bulletin;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.BulletinRepository;
import org.mosqueethonon.service.bulletin.BulletinService;
import org.mosqueethonon.service.inscription.EleveService;
import org.mosqueethonon.v1.dto.bulletin.BulletinDto;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.mapper.bulletin.BulletinMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class BulletinServiceImpl implements BulletinService {

    private EleveService eleveService;

    private BulletinRepository bulletinRepository;

    private BulletinMapper bulletinMapper;

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
        BulletinEntity bulletinEntity = this.bulletinRepository.save(bulletinMapper.fromDtoToEntity(bulletinDto));
        return bulletinMapper.fromEntityToDto(bulletinEntity);
    }

    @Override
    public BulletinDto updateBulletin(Long id, BulletinDto bulletinDto) {
        BulletinEntity bulletinEntity = this.bulletinRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bulletin inexistant ! id = " + id));
        bulletinMapper.updateBulletinEntity(bulletinDto, bulletinEntity);
        return bulletinMapper.fromEntityToDto(this.bulletinRepository.save(bulletinEntity));
    }
}
