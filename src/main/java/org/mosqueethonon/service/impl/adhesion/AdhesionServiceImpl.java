package org.mosqueethonon.service.impl.adhesion;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.adhesion.AdhesionEntity;
import org.mosqueethonon.entity.mail.MailingConfirmationEntity;
import org.mosqueethonon.enums.MailingConfirmationStatut;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.service.adhesion.AdhesionService;
import org.mosqueethonon.v1.dto.adhesion.AdhesionDto;
import org.mosqueethonon.v1.dto.adhesion.AdhesionPatchDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.adhesion.AdhesionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class AdhesionServiceImpl implements AdhesionService {

    private AdhesionRepository adhesionRepository;

    private AdhesionMapper adhesionMapper;

    private MailingConfirmationRepository mailingConfirmationRepository;

    @Override
    public AdhesionDto createAdhesion(AdhesionDto adhesionDto) {
        // Normalisation des chaines de caractères saisies par l'utilisateur
        adhesionDto.normalize();
        AdhesionEntity adhesionEntity = new AdhesionEntity();
        this.adhesionMapper.mapDtoToEntity(adhesionDto, adhesionEntity);
        adhesionEntity.setDateInscription(LocalDateTime.now());
        adhesionEntity.setStatut(StatutInscription.PROVISOIRE);
        adhesionEntity = this.adhesionRepository.save(adhesionEntity);
        adhesionDto = this.adhesionMapper.fromEntityToDto(adhesionEntity);
        this.mailingConfirmationRepository.save(MailingConfirmationEntity.builder().idAdhesion(adhesionEntity.getId())
                .statut(MailingConfirmationStatut.PENDING).build());
        return adhesionDto;
    }

    @Override
    public AdhesionDto findAdhesionById(Long id) {
        Optional<AdhesionEntity> optAdhesionEntity = this.adhesionRepository.findById(id);
        return optAdhesionEntity.map(adhesion -> this.adhesionMapper.fromEntityToDto(adhesion)).orElse(null);
    }

    @Override
    public Set<Long> deleteAdhesions(Set<Long> ids) {
        this.adhesionRepository.deleteAllById(ids);
        return ids;
    }

    @Override
    public Set<Long> patchAdhesions(AdhesionPatchDto adhesionPatchDto) {
        List<AdhesionEntity> adhesionsToUpdate = new ArrayList<>();
        for (Long id : adhesionPatchDto.getIds()) {
            AdhesionEntity adhesion = this.adhesionRepository.findById(id).orElse(null);
            if (adhesion != null) {
                adhesion.setStatut(adhesionPatchDto.getStatut());
                adhesionsToUpdate.add(adhesion);
            }
        }
        if (!CollectionUtils.isEmpty(adhesionsToUpdate)) {
            adhesionsToUpdate = this.adhesionRepository.saveAll(adhesionsToUpdate);
            return adhesionsToUpdate.stream().map(AdhesionEntity::getId).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    @Override
    public AdhesionDto updateAdhesion(Long id, AdhesionDto adhesiondto) {
        AdhesionEntity adhesion = this.adhesionRepository.findById(id).orElse(null);
        if (adhesion == null) {
            throw new IllegalArgumentException("Inscription not found ! idinsc = " + id);
        }
        this.adhesionMapper.mapDtoToEntity(adhesiondto, adhesion);
        adhesion = this.adhesionRepository.save(adhesion);
        return this.adhesionMapper.fromEntityToDto(adhesion);
    }
}
