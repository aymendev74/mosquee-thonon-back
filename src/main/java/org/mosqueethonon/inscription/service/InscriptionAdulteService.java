package org.mosqueethonon.inscription.service;

import org.mosqueethonon.inscription.v1.dto.InscriptionAdulteDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionAdulteParAnneeScolaireDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionAdulteResultDto;
import org.mosqueethonon.inscription.v1.dto.InscriptionSaveCriteria;
import org.mosqueethonon.inscription.v1.dto.ReinscriptionAdulteDto;
import org.mosqueethonon.referentiel.v1.dto.PeriodeDto;

import java.util.List;

public interface InscriptionAdulteService {

    public InscriptionAdulteResultDto createInscription(InscriptionAdulteDto inscription);

    public InscriptionAdulteDto updateInscription(Long id, InscriptionAdulteDto inscription, InscriptionSaveCriteria criteria);

    public InscriptionAdulteDto findInscriptionById(Long id);

    public Integer findNbInscriptionsByPeriode(Long idPeriode);

    public boolean isInscriptionOutsidePeriode(Long idPeriode, PeriodeDto periode);

    public List<InscriptionAdulteParAnneeScolaireDto> findInscriptionsByUtilisateurConnecte();

    public InscriptionAdulteDto reinscription(ReinscriptionAdulteDto reinscriptionAdulteDto);
}
