package org.mosqueethonon.service.inscription;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Set;

public interface InscriptionService {

    public Set<Long> patchInscriptions(JsonNode patchesNode);
    public Set<Long> deleteInscriptions(Set<Long> ids);

}
