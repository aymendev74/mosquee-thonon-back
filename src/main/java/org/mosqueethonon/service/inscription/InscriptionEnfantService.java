package org.mosqueethonon.service.inscription;

import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;

public interface InscriptionEnfantService {

    public InscriptionEnfantDto createInscription(InscriptionEnfantDto inscriptionEnfantDto, InscriptionSaveCriteria criteria);

    public InscriptionEnfantDto updateInscription(Long id, InscriptionEnfantDto inscriptionEnfantDto, InscriptionSaveCriteria criteria);

    public InscriptionEnfantDto findInscriptionById(Long id);

    public Integer findNbInscriptionsByPeriode(Long idPeriode);

    public boolean isInscriptionOutsidePeriode(Long id, PeriodeDto periodeDto);

    public void updateListeAttentePeriode(Long idPeriode);

    public String checkCoherence(Long idInscription, InscriptionEnfantDto inscriptionEnfantDto);

}
