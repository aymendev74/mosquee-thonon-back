package org.mosqueethonon.service.classe;

import org.mosqueethonon.v1.dto.classe.FeuillePresenceDto;

import java.util.List;

public interface IFeuillePresenceService {

    FeuillePresenceDto createFeuillePresence(Long idClasse, FeuillePresenceDto feuillePresence);

    List<FeuillePresenceDto> findFeuillePresencesByClasseId(Long idClasse);

    FeuillePresenceDto updateFeuillePresence(Long id, FeuillePresenceDto feuillePresence);

}
