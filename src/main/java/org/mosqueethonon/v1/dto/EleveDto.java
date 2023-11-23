package org.mosqueethonon.v1.dto;

import lombok.Data;

@Data
public class EleveDto {

    private Long id;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private String niveau;

}
