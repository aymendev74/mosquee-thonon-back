package org.mosqueethonon.inscription.service;

import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantParAnneeScolaireDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionEnfantResultDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionSaveCriteria;
import org.mosqueethonon.referentiel.v1.dto.PeriodeDto;
import java.util.List;

import org.mosqueethonon.inscription.v1.dto.ReinscriptionDto;

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
