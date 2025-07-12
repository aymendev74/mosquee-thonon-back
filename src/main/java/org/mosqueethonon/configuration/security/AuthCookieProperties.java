package org.mosqueethonon.configuration.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "auth.cookie", ignoreUnknownFields = false)
@Data
public class AuthCookieProperties {

    private boolean secure;
    private String path;
    private String name;
    private String sameSite;

}