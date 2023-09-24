package org.mosqueethonon.v1.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PersonneDto {

    private Long id;
    private String nom;
    private String prenom;
    private String dateNaissance;
    private String telephone;
    private String email;
    private String sexe;

}
