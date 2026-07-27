package org.mosqueethonon.referentiel.service;

import org.mosqueethonon.referentiel.entity.PeriodeEntity;
import org.mosqueethonon.referentiel.v1.dto.PeriodeDto;
import org.mosqueethonon.referentiel.v1.dto.PeriodeInfoDto;
import org.mosqueethonon.referentiel.v1.dto.PeriodeValidationResultDto;

import java.util.List;

public interface PeriodeService {

    List<PeriodeInfoDto> findPeriodesByApplication(String application);

    PeriodeDto createPeriode(PeriodeDto periode);

    PeriodeValidationResultDto validatePeriode(Long id, PeriodeDto periode);

    PeriodeDto updatePeriode(Long id, PeriodeDto periode);

    void updateNbMaxElevesIfNeeded(Long idPeriode);

    PeriodeEntity findPeriodeById(Long id);

    void deletePeriode(Long id);

}
