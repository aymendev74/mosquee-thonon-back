package org.mosqueethonon.service;

import java.util.Set;

public interface InscriptionService {

    public Set<Long> validateInscriptions(Set<Long> ids);
    public Set<Long> deleteInscriptions(Set<Long> ids);

}
