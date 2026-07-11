package org.mosqueethonon.service.impl.bulletin;

import lombok.AllArgsConstructor;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.entity.bulletin.BulletinMatiereEntity;
import org.mosqueethonon.entity.document.DocumentEntity;
import org.mosqueethonon.entity.referentiel.MatiereEntity;
import org.mosqueethonon.enums.DocumentMetadataKey;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.enums.MatiereEnum;
import org.mosqueethonon.enums.TypeMatiereEnum;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.BulletinRepository;
import org.mosqueethonon.repository.DocumentRepository;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.repository.MatiereRepository;
import org.mosqueethonon.service.bulletin.BulletinService;
import org.mosqueethonon.service.document.AsyncDocumentService;
import org.mosqueethonon.service.document.DocumentService;
import org.mosqueethonon.service.inscription.EleveService;
import org.mosqueethonon.service.referentiel.MatiereService;
import org.mosqueethonon.v1.dto.bulletin.BulletinDto;
import org.mosqueethonon.v1.dto.bulletin.BulletinMatiereDto;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.mapper.bulletin.BulletinMapper;
import org.mosqueethonon.v1.mapper.bulletin.BulletinMatiereMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class BulletinServiceImpl implements BulletinService {

    private EleveService eleveService;

    private BulletinRepository bulletinRepository;

    private BulletinMapper bulletinMapper;

    private BulletinMatiereMapper bulletinMatiereMapper;

    private MatiereService matiereService;

    private MatiereRepository matiereRepository;

    private AsyncDocumentService asyncDocumentService;

    private DocumentRequestRepository documentRequestRepository;

    private DocumentRepository documentRepository;

    private DocumentService documentService;

    @Override
    public List<BulletinDto> findBulletinsByIdEleve(Long idEleve) {
        EleveDto eleve = this.eleveService.findEleveById(idEleve);
        if (eleve == null) {
            throw new ResourceNotFoundException("Eleve inexistant ! id = " + idEleve);
        }
        List<BulletinEntity> bulletins = this.bulletinRepository.findByIdEleve(idEleve);
        List<MatiereEnum> codesMatieresEnfant = this.getCodesMatieresEnfant();
        return bulletins.stream().map(bulletinEntity -> {
            BulletinDto dto = bulletinMapper.fromEntityToDto(bulletinEntity);
            dto.setComplet(dto.calculerCompletude(codesMatieresEnfant));
            this.findDocumentByBulletinId(bulletinEntity.getId()).ifPresent(doc -> dto.setIdDocument(doc.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public BulletinDto createBulletin(BulletinDto bulletinDto) {
        BulletinEntity bulletinEntity = bulletinMapper.fromDtoToEntity(bulletinDto);
        bulletinEntity.setBulletinMatieres(this.mapBulletinMatieres(bulletinDto.getBulletinMatieres()));
        BulletinDto saved = bulletinMapper.fromEntityToDto(this.bulletinRepository.save(bulletinEntity));
        saved.setComplet(saved.calculerCompletude(this.getCodesMatieresEnfant()));
        if (Boolean.TRUE.equals(saved.getComplet())) {
            this.asyncDocumentService.requestDocumentGeneration(DocumentRequestType.BULLETIN, saved.getId());
        }
        this.findDocumentByBulletinId(saved.getId()).ifPresent(doc -> saved.setIdDocument(doc.getId()));
        return saved;
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
        BulletinDto saved = bulletinMapper.fromEntityToDto(this.bulletinRepository.save(bulletinEntity));
        saved.setComplet(saved.calculerCompletude(this.getCodesMatieresEnfant()));
        if (Boolean.TRUE.equals(saved.getComplet())) {
            this.asyncDocumentService.requestDocumentGeneration(DocumentRequestType.BULLETIN, saved.getId());
        }
        this.findDocumentByBulletinId(saved.getId()).ifPresent(doc -> saved.setIdDocument(doc.getId()));
        return saved;
    }

    private List<MatiereEnum> getCodesMatieresEnfant() {
        return this.matiereRepository.findByType(TypeMatiereEnum.ENFANT).stream()
                .map(MatiereEntity::getCode)
                .collect(Collectors.toList());
    }

    @Override
    public boolean verifierCompletude(BulletinDto bulletinDto) {
        return bulletinDto.calculerCompletude(this.getCodesMatieresEnfant());
    }

    @Override
    @Transactional
    public void deleteBulletin(Long id) {
        this.documentRequestRepository.deleteByTypeAndBusinessIdIn(DocumentRequestType.BULLETIN, Sets.newHashSet(id));
        this.findDocumentByBulletinId(id).ifPresent(doc -> this.documentService.deleteDocument(doc.getId()));
        this.bulletinRepository.deleteById(id);
    }

    @Override
    public Optional<DocumentEntity> findDocumentByBulletinId(Long bulletinId) {
        return this.documentRepository.findByMetadataKeyAndValue(DocumentMetadataKey.ID_BULLETIN, String.valueOf(bulletinId));
    }

}
