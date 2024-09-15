package org.mosqueethonon.service.inscription;

import org.mosqueethonon.v1.dto.inscription.InscriptionPatchDto;

import java.util.Set;

public interface InscriptionService {

    public Set<Long> patchInscriptions(InscriptionPatchDto inscriptionPatchDto);
    public Set<Long> deleteInscriptions(Set<Long> ids);

}
