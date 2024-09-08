package org.mosqueethonon.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.PeriodeEntity;
import org.mosqueethonon.entity.PeriodeInfoEntity;
import org.mosqueethonon.repository.PeriodeInfoRepository;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.service.InscriptionEnfantService;
import org.mosqueethonon.service.PeriodeService;
import org.mosqueethonon.utils.DateUtils;
import org.mosqueethonon.v1.dto.PeriodeDto;
import org.mosqueethonon.v1.dto.PeriodeInfoDto;
import org.mosqueethonon.v1.dto.PeriodeValidationResultDto;
import org.mosqueethonon.v1.mapper.PeriodeInfoMapper;
import org.mosqueethonon.v1.mapper.PeriodeMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PeriodeServiceImpl implements PeriodeService {

    private PeriodeInfoRepository periodeInfoRepository;
    private PeriodeRepository periodeRepository;
    private PeriodeInfoMapper periodeInfoMapper;
    private PeriodeMapper periodeMapper;

    private InscriptionEnfantService inscriptionEnfantService;

    private static final String PERIODE_APPLICATION_COURS = "COURS";

    @Override
    public List<PeriodeInfoDto> findPeriodesByApplication(String application) {
        List<PeriodeInfoEntity> periodeEntities = this.periodeInfoRepository.findByApplicationOrderByDateDebutDesc(application);
        if(!CollectionUtils.isEmpty(periodeEntities)) {
            return periodeEntities.stream().map(this.periodeInfoMapper::fromEntityToDto).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Transactional
    @Override
    public PeriodeDto savePeriode(PeriodeDto periode) {
        PeriodeEntity periodeEntity = this.periodeRepository.save(this.periodeMapper.fromDtoToEntity(periode));
        this.inscriptionEnfantService.updateListeAttentePeriode(periode.getId());
        return this.periodeMapper.fromEntityToDto(periodeEntity);
    }

    @Override
    public PeriodeValidationResultDto validatePeriode(PeriodeDto periode) {
        // Il ne doit y avoir aucun chevauchement entre cette periode et une autre existante
        boolean successValidation = this.checkNoOverlap(periode);
        if(!successValidation) {
            return PeriodeValidationResultDto.builder().success(false).errorCode("OVERLAP").build();
        }

        // Il ne doit y avoir aucune inscription enregistrée qui se retrouve en dehors de la période (dateInscription)
        successValidation = this.checkNoInscriptionOutsidePeriode(periode);
        if(!successValidation) {
            return PeriodeValidationResultDto.builder().success(false).errorCode("INSCRIPTION_OUTSIDE").build();
        }

        // Contrôle du nombre d'inscription maximum (par rapport au nombre déjà inscrit sur la période)
        successValidation = this.checkNbInscriptionVsMaxPeriode(periode);
        if(!successValidation) {
            return PeriodeValidationResultDto.builder().success(false).errorCode("NB_MAX_INSCRIPTION").build();
        }

        return PeriodeValidationResultDto.builder().periode(periode).success(true).build();
    }

    private boolean checkNbInscriptionVsMaxPeriode(PeriodeDto periode) {
        if(periode.getId() != null && periode.getNbMaxInscription() != null) {
            Integer nbInscription = inscriptionEnfantService.findNbInscriptionsByPeriode(periode.getId());
            if(nbInscription != null) {
                return nbInscription <= periode.getNbMaxInscription();
            }
        }
        return true;
    }

    private boolean checkNoInscriptionOutsidePeriode(PeriodeDto periode) {
        return !this.inscriptionEnfantService.isInscriptionOutsideRange(periode);
    }

    private boolean checkNoOverlap(PeriodeDto periode) {
        List<PeriodeEntity> periodes = null;
        if(periode.getId() == null) { // si création on ramène toutes les périodes
            periodes = this.periodeRepository.findByApplication(PERIODE_APPLICATION_COURS);
        } else { // sinon, toutes sauf celle qu'on est en train de valider
            periodes = this.periodeRepository.findByApplicationAndIdNot(PERIODE_APPLICATION_COURS, periode.getId());
        }
        Optional<PeriodeEntity> optPeriodeOverlaps = periodes.stream().filter(existingPeriode -> DateUtils.isOverlap(existingPeriode.getDateDebut(), existingPeriode.getDateFin(),
                periode.getDateDebut(), periode.getDateFin())).findFirst();
        return !optPeriodeOverlaps.isPresent();
    }
}
