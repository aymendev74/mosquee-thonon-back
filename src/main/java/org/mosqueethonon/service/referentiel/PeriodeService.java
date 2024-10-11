package org.mosqueethonon.service.referentiel;

import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;
import org.mosqueethonon.v1.dto.referentiel.PeriodeInfoDto;
import org.mosqueethonon.v1.dto.referentiel.PeriodeValidationResultDto;

import java.util.List;

public interface PeriodeService {

    List<PeriodeInfoDto> findPeriodesByApplication(String application);

    PeriodeDto createPeriode(PeriodeDto periode);

    PeriodeValidationResultDto validatePeriode(Long id, PeriodeDto periode);

    PeriodeDto updatePeriode(Long id, PeriodeDto periode);


}
