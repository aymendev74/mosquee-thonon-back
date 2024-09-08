package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.PeriodeDto;
import org.mosqueethonon.v1.dto.PeriodeInfoDto;
import org.mosqueethonon.v1.dto.PeriodeValidationResultDto;

import java.util.List;

public interface PeriodeService {

    List<PeriodeInfoDto> findPeriodesByApplication(String application);

    PeriodeDto savePeriode(PeriodeDto periode);

    PeriodeValidationResultDto validatePeriode(PeriodeDto periode);

}
