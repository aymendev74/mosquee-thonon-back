package org.mosqueethonon.v1.criterias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCriteria {

    private String nom;
    private String prenom;
    private String email;
    private String role;

}
