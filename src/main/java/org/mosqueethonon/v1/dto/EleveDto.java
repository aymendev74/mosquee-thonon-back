package org.mosqueethonon.v1.dto;

import lombok.Data;
import org.mosqueethonon.entity.Signature;

@Data
public class EleveDto {

    private Long id;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private String niveau;
    private Long idTarif;
    private Signature signature;

}
