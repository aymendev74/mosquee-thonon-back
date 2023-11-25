package org.mosqueethonon.service.impl;

import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.InscriptionEntity;
import org.mosqueethonon.repository.InscriptionRepository;
import org.mosqueethonon.service.InscriptionService;
import org.mosqueethonon.service.TarifCalculService;
import org.mosqueethonon.v1.dto.InscriptionDto;
import org.mosqueethonon.v1.dto.InscriptionInfosDto;
import org.mosqueethonon.v1.dto.TarifInscriptionDto;
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
    private TarifCalculService tarifCalculService;

    @Override
    public InscriptionDto savePersonne(InscriptionDto inscription) {
        this.doCalculTarifInscription(inscription);
        InscriptionEntity entity = this.inscriptionMapper.fromDtoToEntity(inscription);
        entity = this.inscriptionRepository.save(entity);
        return this.inscriptionMapper.fromEntityToDto(entity);
    }

    private void doCalculTarifInscription(InscriptionDto inscription) {
        InscriptionInfosDto inscriptionInfos = InscriptionInfosDto.builder().eleves(inscription.getEleves())
                .responsableLegal(inscription.getResponsableLegal()).build();
        TarifInscriptionDto tarifs = this.tarifCalculService.calculTarifInscription(inscriptionInfos);
        inscription.getResponsableLegal().setIdTarif(tarifs.getIdTariBase());
        inscription.getEleves().forEach(eleve -> eleve.setIdTarif(tarifs.getIdTariEleve()));
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

    @Override
    public List<Long> deleteInscriptions(List<Long> ids) {
        this.inscriptionRepository.deleteAllById(ids);
        return ids;
    }
}
