package org.mosqueethonon.utilisateur.v1.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountInfosDto {

    private String username;
    private String prenom;
    private boolean enabled;

}
