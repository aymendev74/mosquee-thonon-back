package org.mosqueethonon.v1.dto;

import lombok.Data;
import org.mosqueethonon.v1.enums.StatutInscription;

import java.util.List;

@Data
public class InscriptionPatchDto {

    private List<Long> ids;
    private StatutInscription statut;

}
