package org.mosqueethonon.configuration.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
@Validated
@Data
public class ApplicationConfiguration {

    @NotBlank
    private String tokenUri;
    @NotBlank
    private String loginRedirectUri;
    @NotBlank
    private String logoutRedirectUri;
    @NotEmpty
    private List<String> allowedOrigins;
    @NotBlank
    private String issuerUri;
    @NotBlank
    private String resetPasswordUri;
    @NotBlank
    private String jwtDecoderUri;
    @NotBlank
    private String activationUtilisateurUri;
    @NotNull
    private Long resourceLockTimeout;
    @Valid
    @NotNull
    private RibAmc ribAmc;
    @Valid
    @NotNull
    private Documents documents;

    @Data
    public static final class RibAmc {
        @NotBlank
        private String fileLocation;
        @NotBlank
        private String mailAttachmentFilename;
    }

    @Data
    public static final class Documents {
        @NotBlank
        private String basePath;
    }

}
