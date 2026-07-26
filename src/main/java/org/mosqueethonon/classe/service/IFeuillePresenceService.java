package org.mosqueethonon.classe.service;

import org.mosqueethonon.classe.v1.dto.FeuillePresenceDto;

import java.util.List;

public interface IFeuillePresenceService {

    FeuillePresenceDto createFeuillePresence(Long idClasse, FeuillePresenceDto feuillePresence);

    List<FeuillePresenceDto> findFeuillePresencesByClasseId(Long idClasse);

    FeuillePresenceDto updateFeuillePresence(Long id, FeuillePresenceDto feuillePresence);

    void deleteFeuillePresence(Long id);

}
