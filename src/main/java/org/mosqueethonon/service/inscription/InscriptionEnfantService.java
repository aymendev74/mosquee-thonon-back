package org.mosqueethonon.service.inscription;

import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;
import java.util.List;

public interface InscriptionEnfantService {

    public InscriptionEnfantDto createInscription(InscriptionEnfantDto inscriptionEnfantDto);

    public InscriptionEnfantDto updateInscription(Long id, InscriptionEnfantDto inscriptionEnfantDto, InscriptionSaveCriteria criteria);

    public InscriptionEnfantDto findInscriptionById(Long id);

    public Integer findNbInscriptionsByPeriode(Long idPeriode);

    public boolean isInscriptionOutsidePeriode(Long id, PeriodeDto periodeDto);

    public String checkCoherence(Long idInscription, InscriptionEnfantDto inscriptionEnfantDto);

    //public Integer getLastPositionAttenteByPeriode(Long idPeriode);

    Integer getNbElevesInscritsByIdPeriode(Long idPeriode);

    //List<InscriptionEnfantDto> getInscriptionEnAttenteByPeriode(Long idPeriode);

    //List<InscriptionEnfantDto> updateInscriptions(List<InscriptionEnfantDto> inscriptions);

    void updateListeAttente(Long idPeriode, Integer nbMaxInscriptions);

}
