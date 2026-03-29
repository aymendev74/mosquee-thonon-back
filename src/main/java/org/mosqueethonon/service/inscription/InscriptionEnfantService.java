package org.mosqueethonon.service.inscription;

import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantParAnneeScolaireDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionEnfantResultDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;
import java.util.List;

import org.mosqueethonon.v1.dto.inscription.ReinscriptionDto;

public interface InscriptionEnfantService {

    public InscriptionEnfantResultDto createInscription(InscriptionEnfantDto inscriptionEnfantDto);

    List<InscriptionEnfantParAnneeScolaireDto> findInscriptionsByUtilisateurConnecte();

    InscriptionEnfantDto reinscription(ReinscriptionDto reinscriptionDto);

    public InscriptionEnfantDto updateInscription(Long id, InscriptionEnfantDto inscriptionEnfantDto, InscriptionSaveCriteria criteria);

    public InscriptionEnfantDto findInscriptionById(Long id);

    public Integer findNbInscriptionsByPeriode(Long idPeriode);

    public boolean isInscriptionOutsidePeriode(Long id, PeriodeDto periodeDto);

    public String checkCoherence(Long idInscription, InscriptionEnfantDto inscriptionEnfantDto);

    Integer getNbElevesInscritsByIdPeriode(Long idPeriode);

    void updateListeAttente(Long idPeriode, Integer nbMaxInscriptions);

}
