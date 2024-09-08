package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.PeriodeDto;
import java.util.Set;

public interface InscriptionEnfantService {

    public InscriptionEnfantDto saveInscription(InscriptionEnfantDto personne, InscriptionSaveCriteria criteria);
    public InscriptionEnfantDto findInscriptionById(Long id);

    public Integer findNbInscriptionsByPeriode(Long idPeriode);

    public boolean isInscriptionOutsideRange(PeriodeDto periodeDto);
    public void updateListeAttentePeriode(Long idPeriode);

    public String checkCoherence(InscriptionEnfantDto inscriptionEnfantDto);

}
