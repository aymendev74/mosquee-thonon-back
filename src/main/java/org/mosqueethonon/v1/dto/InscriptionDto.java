package org.mosqueethonon.v1.dto;

import lombok.Data;
import org.mosqueethonon.entity.Signature;
import org.mosqueethonon.v1.enums.StatutInscription;
import java.util.List;

@Data
public class InscriptionDto {

    private Long id;
    private StatutInscription statut;
    private String dateInscription;
    private ResponsableLegalDto responsableLegal;
    private List<EleveDto> eleves;
    private Signature signature;

}
