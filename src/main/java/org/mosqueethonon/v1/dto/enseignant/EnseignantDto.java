package org.mosqueethonon.v1.dto.enseignant;

import lombok.Data;

@Data
public class EnseignantDto {

    private Long id;
    private String username;
    private String nom;
    private String prenom;
    private String mobile;
    private Boolean hasClasse;

}
