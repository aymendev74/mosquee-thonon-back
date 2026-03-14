package org.mosqueethonon.service.inscription;

import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteParAnneeScolaireDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionAdulteResultDto;
import org.mosqueethonon.v1.dto.inscription.InscriptionSaveCriteria;
import org.mosqueethonon.v1.dto.inscription.ReinscriptionAdulteDto;
import org.mosqueethonon.v1.dto.referentiel.PeriodeDto;

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
