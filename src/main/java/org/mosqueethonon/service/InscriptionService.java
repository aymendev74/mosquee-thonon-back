package org.mosqueethonon.service;

import org.mosqueethonon.v1.dto.InscriptionPatchDto;

import java.util.Set;

public interface InscriptionService {

    public Set<Long> patchInscriptions(InscriptionPatchDto inscriptionPatchDto);
    public Set<Long> deleteInscriptions(Set<Long> ids);

}
