package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.PeriodeDto;
import org.mosqueethonon.v1.dto.PeriodeInfoDto;

import java.util.List;

public interface PeriodeService {

    List<PeriodeInfoDto> findAllPeriodes();

    PeriodeDto savePeriode(PeriodeDto periode);

}
