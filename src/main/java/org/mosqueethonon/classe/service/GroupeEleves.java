package org.mosqueethonon.classe.service;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.inscription.entity.EleveEntity;
import org.mosqueethonon.classe.enums.JourActiviteEnum;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class GroupeEleves {

    /**
     * @Builder.Default est indispensable : sans lui, Lombok ignore l'initialisation et
     * GroupeEleves.builder().build() laisse la liste à null. ClasseServiceImpl fait
     * groupeEleve.getEleves().add(...) sans contrôle, ce qui produisait un NullPointerException
     * sur POST /v1/classes/auto dès qu'un élève était inscrit.
     */
    @Builder.Default
    private List<EleveEntity> eleves = new ArrayList<>();

    private JourActiviteEnum jourClasse;

}
