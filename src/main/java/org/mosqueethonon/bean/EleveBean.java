package org.mosqueethonon.bean;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.enums.NiveauInterneEnum;

@Builder
@Data
public class EleveBean {

    private Long idEleve;
    private Long idInscription;
    private NiveauInterneEnum niveauInterne;

}
