package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.InscriptionDto;
import org.mosqueethonon.v1.dto.PeriodeDto;

import java.util.Set;

public interface InscriptionService {

    public InscriptionDto saveInscription(InscriptionDto personne);
    public InscriptionDto findInscriptionById(Long id);
    public Set<Long> validateInscriptions(Set<Long> ids);
    public Set<Long> deleteInscriptions(Set<Long> ids);

    public Integer findNbInscriptionsByPeriode(Long idPeriode);

    public boolean isInscriptionOutsideRange(PeriodeDto periodeDto);
    public void updateListeAttentePeriode(Long idPeriode);

}
