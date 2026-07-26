package org.mosqueethonon.classe.service;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.inscription.entity.EleveEntity;
import org.mosqueethonon.classe.enums.JourActiviteEnum;
import java.util.List;

@Builder
@Data
public class GroupeElevesBean {

    private List<EleveEntity> eleves;
    private JourActiviteEnum jourClasse;

}
