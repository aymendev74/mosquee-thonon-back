package org.mosqueethonon.v1.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class MailObjectDto {

    private String nom;
    private String prenom;
    private String email;

}
