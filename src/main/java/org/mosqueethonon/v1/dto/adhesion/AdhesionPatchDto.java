package org.mosqueethonon.v1.dto.adhesion;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.util.List;

@Data
public class AdhesionPatchDto {

    private List<Long> ids;
    private StatutInscription statut;

}
