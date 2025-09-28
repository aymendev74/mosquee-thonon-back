package org.mosqueethonon.configuration.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
@Data
public class ApplicationProperties {

    private String tokenUri;
    private String loginRedirectUri;
    private String logoutRedirectUri;
    private List<String> allowedOrigins;
    private String issuerUri;
    private String jwtDecoderUri;

}
