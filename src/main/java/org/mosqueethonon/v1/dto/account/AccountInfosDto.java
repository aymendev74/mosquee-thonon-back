package org.mosqueethonon.v1.dto.account;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountInfosDto {

    private String username;
    private boolean enabled;

}
