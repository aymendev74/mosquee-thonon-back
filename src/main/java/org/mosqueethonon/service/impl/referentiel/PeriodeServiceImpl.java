package org.mosqueethonon.service.impl.referentiel;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mosqueethonon.entity.referentiel.PeriodeEntity;
import org.mosqueethonon.entity.referentiel.PeriodeInfoEntity;
import org.mosqueethonon.repository.PeriodeInfoRepository;
import org.mosqueethonon.repository.PeriodeRepository;
import org.mosqueethonon.service.inscription.InscriptionAdulteService;
import org.mosqueethonon.service.inscription.InscriptionEnfantService;
import org.mosqueethonon.service.referentiel.PeriodeService;
import org.mosqueethonon.utils.DateUtils;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;
import org.mosqueethonon.v1.dto.referentiel.PeriodeInfoDto;
import org.mosqueethonon.v1.dto.referentiel.PeriodeValidationResultDto;
import org.mosqueethonon.v1.mapper.referentiel.PeriodeInfoMapper;
import org.mosqueethonon.v1.mapper.referentiel.PeriodeMapper;
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

    private InscriptionAdulteService inscriptionAdulteService;

    private static final String APPLICATION_COURS_ENFANT = "COURS_ENFANT";

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
    public PeriodeDto createPeriode(PeriodeDto periode) {
        PeriodeEntity periodeEntity = new PeriodeEntity();
        periodeEntity.setIdPeriodePrecedente(this.getLastPeriodeId(periode.getApplication()));
        this.periodeRepository.save(this.periodeMapper.mapDtoToEntity(periode, periodeEntity));
        return this.periodeMapper.fromEntityToDto(periodeEntity);
    }

    private Long getLastPeriodeId(String application) {
        PeriodeEntity periode = this.periodeRepository.findFirstByApplicationOrderByDateDebutDesc(application);
        if(periode != null) {
            return periode.getId();
        }
        return null;
    }

    @Override
    @Transactional
    public PeriodeDto updatePeriode(Long id, PeriodeDto periode) {
        PeriodeEntity periodeEntity = this.periodeRepository.findById(id).orElse(null);
        if(periodeEntity == null) {
            throw new IllegalArgumentException("Periode non trouvée ! idperi = " + id);
        }
        this.periodeMapper.mapDtoToEntity(periode, periodeEntity);
        periodeEntity = this.periodeRepository.save(periodeEntity);
        this.inscriptionEnfantService.updateListeAttentePeriode(id);
        return this.periodeMapper.fromEntityToDto(periodeEntity);
    }

    @Override
    public PeriodeValidationResultDto validatePeriode(Long id, PeriodeDto periode) {
        // Il ne doit y avoir aucun chevauchement entre cette periode et une autre existante
        boolean successValidation = this.checkNoOverlap(id, periode);
        if(!successValidation) {
            return PeriodeValidationResultDto.builder().success(false).errorCode("OVERLAP").build();
        }

        // Il ne doit y avoir aucune inscription enregistrée qui se retrouve en dehors de la période (dateInscription)
        if(id != null) {
            successValidation = this.checkNoInscriptionOutsidePeriode(id, periode);
            if(!successValidation) {
                return PeriodeValidationResultDto.builder().success(false).errorCode("INSCRIPTION_OUTSIDE").build();
            }

            // Contrôle du nombre d'inscription maximum (par rapport au nombre déjà inscrit sur la période)
            successValidation = this.checkNbInscriptionVsMaxPeriode(id, periode);
            if(!successValidation) {
                return PeriodeValidationResultDto.builder().success(false).errorCode("NB_MAX_INSCRIPTION").build();
            }
        }

        return PeriodeValidationResultDto.builder().periode(periode).success(true).build();
    }

    private boolean checkNbInscriptionVsMaxPeriode(Long id, PeriodeDto periode) {
        if(id != null && periode.getNbMaxInscription() != null) {
            Integer nbInscription = null;
            if(APPLICATION_COURS_ENFANT.equals(periode.getApplication())) {
                nbInscription = inscriptionEnfantService.findNbInscriptionsByPeriode(id);
            } else {
                nbInscription = inscriptionAdulteService.findNbInscriptionsByPeriode(id);
            }
            if(nbInscription != null && periode.getNbMaxInscription() != null) {
                return nbInscription <= periode.getNbMaxInscription();
            }
        }
        return true;
    }

    private boolean checkNoInscriptionOutsidePeriode(Long id, PeriodeDto periode) {
        if (periode.getApplication().equals(APPLICATION_COURS_ENFANT)) {
            return !this.inscriptionEnfantService.isInscriptionOutsidePeriode(id, periode);
        } else {
            return !this.inscriptionAdulteService.isInscriptionOutsidePeriode(id, periode);
        }
    }

    private boolean checkNoOverlap(Long id, PeriodeDto periode) {
        List<PeriodeEntity> periodes = null;
        if(id == null) { // si création on ramène toutes les périodes
            periodes = this.periodeRepository.findByApplication(periode.getApplication());
        } else { // sinon, toutes sauf celle qu'on est en train de valider
            periodes = this.periodeRepository.findByApplicationAndIdNot(periode.getApplication(), id);
        }
        Optional<PeriodeEntity> optPeriodeOverlaps = periodes.stream().filter(existingPeriode -> DateUtils.isOverlap(existingPeriode.getDateDebut(), existingPeriode.getDateFin(),
                periode.getDateDebut(), periode.getDateFin())).findFirst();
        return !optPeriodeOverlaps.isPresent();
    }
}
