package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.AdhesionEntity;
import org.mosqueethonon.entity.InscriptionEntity;
import org.mosqueethonon.enums.TypeMailEnum;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.service.AdhesionService;
import org.mosqueethonon.service.MailService;
import org.mosqueethonon.v1.dto.AdhesionDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.AdhesionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class AdhesionServiceImpl implements AdhesionService {

    private AdhesionRepository adhesionRepository;

    private AdhesionMapper adhesionMapper;

    private MailService mailService;

    @Override
    public AdhesionDto saveAdhesion(AdhesionDto adhesionDto) {
        // Si nouvelle adhésion alors on envoi un mail de confirmation à l'adhérent
        boolean sendMailAdherent = adhesionDto.getId() == null;
        AdhesionEntity adhesionEntity = this.adhesionMapper.fromDtoToEntity(adhesionDto);

        if(adhesionEntity.getDateInscription() == null) {
            adhesionEntity.setDateInscription(LocalDate.now());
        }
        if(adhesionEntity.getStatut() == null) {
            adhesionEntity.setStatut(StatutInscription.PROVISOIRE);
        }

        adhesionEntity = this.adhesionRepository.save(adhesionEntity);
        adhesionDto = this.adhesionMapper.fromEntityToDto(adhesionEntity);

        if(sendMailAdherent) {
            this.mailService.sendEmailConfirmation(adhesionDto, TypeMailEnum.ADHESION, null);
        }
        return adhesionDto;
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
    public Set<Long> deleteAdhesions(Set<Long> ids) {
        this.adhesionRepository.deleteAllById(ids);
        return ids;
    }

    @Override
    public Set<Long> validateAdhesions(Set<Long> ids) {
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
            return adhesionsToUpdate.stream().map(AdhesionEntity::getId).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }
}
