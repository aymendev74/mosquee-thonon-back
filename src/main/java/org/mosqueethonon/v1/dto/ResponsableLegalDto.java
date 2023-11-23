package org.mosqueethonon.v1.dto;

import lombok.Data;
import org.mosqueethonon.entity.Signature;

@Data
public class ResponsableLegalDto {

    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private String mobile;
    private String email;
    private String numeroEtRue;
    private Integer codePostal;
    private String ville;
    private Signature signature;

}
