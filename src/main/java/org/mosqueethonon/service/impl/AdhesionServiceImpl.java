package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.AdhesionEntity;
import org.mosqueethonon.entity.MailingConfirmationEntity;
import org.mosqueethonon.enums.MailingConfirmationStatut;
import org.mosqueethonon.repository.AdhesionRepository;
import org.mosqueethonon.repository.MailingConfirmationRepository;
import org.mosqueethonon.service.AdhesionService;
import org.mosqueethonon.v1.dto.AdhesionDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.AdhesionMapper;
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
    public AdhesionDto saveAdhesion(AdhesionDto adhesionDto) {
        // Normalisation des chaines de caractères saisies par l'utilisateur
        adhesionDto.normalize();
        // Si nouvelle adhésion alors on envoi un mail de confirmation à l'adhérent
        boolean sendMailAdherent = adhesionDto.getId() == null;
        AdhesionEntity adhesionEntity = this.adhesionMapper.fromDtoToEntity(adhesionDto);

        if(adhesionEntity.getDateInscription() == null) {
            adhesionEntity.setDateInscription(LocalDateTime.now());
        }
        if(adhesionEntity.getStatut() == null) {
            adhesionEntity.setStatut(StatutInscription.PROVISOIRE);
        }

        adhesionEntity = this.adhesionRepository.save(adhesionEntity);
        adhesionDto = this.adhesionMapper.fromEntityToDto(adhesionEntity);

        if(sendMailAdherent) {
            this.mailingConfirmationRepository.save(MailingConfirmationEntity.builder().idAdhesion(adhesionDto.getId())
                    .statut(MailingConfirmationStatut.PENDING).build());
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
