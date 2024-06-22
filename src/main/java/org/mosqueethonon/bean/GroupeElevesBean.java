package org.mosqueethonon.bean;

import lombok.Builder;
import lombok.Data;
import org.mosqueethonon.enums.NiveauInterneEnum;

import java.util.List;

@Builder
@Data
public class GroupeElevesBean {

    private List<EleveBean> eleveBeans;
    private List<NiveauInterneEnum> niveaux;

}
