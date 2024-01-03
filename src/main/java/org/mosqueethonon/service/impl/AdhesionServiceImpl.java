package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.AdhesionEntity;
import org.mosqueethonon.entity.InscriptionEntity;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.service.AdhesionService;
import org.mosqueethonon.v1.dto.AdhesionDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.AdhesionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class AdhesionServiceImpl implements AdhesionService {

    private AdhesionRepository adhesionRepository;

    private AdhesionMapper adhesionMapper;

    @Override
    public AdhesionDto saveAdhesion(AdhesionDto adhesionDto) {
        AdhesionEntity adhesionEntity = this.adhesionMapper.fromDtoToEntity(adhesionDto);
        if(adhesionEntity.getDateInscription() == null) {
            adhesionEntity.setDateInscription(LocalDate.now());
        }
        if(adhesionEntity.getStatut() == null) {
            adhesionEntity.setStatut(StatutInscription.PROVISOIRE);
        }
        adhesionEntity = this.adhesionRepository.save(adhesionEntity);
        return this.adhesionMapper.fromEntityToDto(adhesionEntity);
    }

    @Override
    public AdhesionDto findAdhesionById(Long id) {
        Optional<AdhesionEntity> optAdhesionEntity = this.adhesionRepository.findById(id);
        if(optAdhesionEntity.isPresent()) {
            return this.adhesionMapper.fromEntityToDto(optAdhesionEntity.get());
        }
        return null;
    }

    @Override
    public List<Long> deleteAdhesions(List<Long> ids) {
        this.adhesionRepository.deleteAllById(ids);
        return ids;
    }

    @Override
    public List<Long> validateInscriptions(List<Long> ids) {
        List<AdhesionEntity> adhesionsToUpdate = new ArrayList<>();
        for (Long id : ids) {
            AdhesionEntity adhesion = this.adhesionRepository.findById(id).orElse(null);
            if(adhesion!=null) {
                adhesion.setStatut(StatutInscription.VALIDEE);
                adhesionsToUpdate.add(adhesion);
            }
        }
        if(!CollectionUtils.isEmpty(adhesionsToUpdate)) {
            adhesionsToUpdate = this.adhesionRepository.saveAll(adhesionsToUpdate);
            return adhesionsToUpdate.stream().map(AdhesionEntity::getId).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
