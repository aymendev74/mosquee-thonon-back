package org.mosqueethonon.service.classe;

import org.mosqueethonon.v1.dto.classe.FeuillePresenceDto;

public interface IFeuillePresenceService {

    FeuillePresenceDto createFeuillePresence(Long idClasse, FeuillePresenceDto feuillePresence);

}
