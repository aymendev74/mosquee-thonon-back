package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.InscriptionEntity;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.repository.specifications.InscriptionEntitySpecifications;
import org.mosqueethonon.service.InscriptionService;
import org.mosqueethonon.service.criteria.InscriptionCriteria;
import org.mosqueethonon.v1.dto.InscriptionDto;
import org.mosqueethonon.v1.enums.StatutInscription;
import org.mosqueethonon.v1.mapper.InscriptionMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InscriptionServiceImpl implements InscriptionService {

    private InscriptionRepository inscriptionRepository;
    private InscriptionMapper inscriptionMapper;

    @Override
    public InscriptionDto savePersonne(InscriptionDto personne) {
        InscriptionEntity entity = this.inscriptionMapper.fromDtoToEntity(personne);
        entity = this.inscriptionRepository.save(entity);
        return this.inscriptionMapper.fromEntityToDto(entity);
    }

    @Override
    public List<InscriptionDto> findPersonneByCriteria(InscriptionCriteria criteria) {
        List<InscriptionEntity> personnes = this.inscriptionRepository.findAll(InscriptionEntitySpecifications.withCriteria(criteria));
        if(!CollectionUtils.isEmpty(personnes)) {
            return personnes.stream().map(this.inscriptionMapper::fromEntityToDto).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public InscriptionDto findInscriptionById(Long id) {
        InscriptionEntity inscriptionEntity = this.inscriptionRepository.findById(id).orElse(null);
        if(inscriptionEntity !=null) {
            return this.inscriptionMapper.fromEntityToDto(inscriptionEntity);
        }
        return null;
    }

    @Override
    public List<Long> validateInscriptions(List<Long> ids) {
        List<InscriptionEntity> inscriptionsToUpdate = new ArrayList<>();
        for (Long id : ids) {
            InscriptionEntity personne = this.inscriptionRepository.findById(id).orElse(null);
            if(personne!=null) {
                personne.setStatut(StatutInscription.VALIDEE);
                inscriptionsToUpdate.add(personne);
            }
        }
        if(!CollectionUtils.isEmpty(inscriptionsToUpdate)) {
            inscriptionsToUpdate = this.inscriptionRepository.saveAll(inscriptionsToUpdate);
            return inscriptionsToUpdate.stream().map(InscriptionEntity::getId).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

}
