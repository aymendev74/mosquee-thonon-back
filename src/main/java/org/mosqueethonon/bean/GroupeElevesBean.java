package org.mosqueethonon.bean;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.entity.inscription.EleveEntity;
import org.mosqueethonon.enums.JourActiviteEnum;
import java.util.List;

@Builder
@Data
public class GroupeElevesBean {

    private List<EleveEntity> eleves;
    private JourActiviteEnum jourClasse;

}
