package org.mosqueethonon.utilisateur.v1.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String oldPassword;
    private String newPassword;

}
