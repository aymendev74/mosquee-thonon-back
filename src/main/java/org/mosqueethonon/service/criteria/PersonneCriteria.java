package org.mosqueethonon.service.criteria;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PersonneCriteria {

    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String telephone;
    private String email;

}
