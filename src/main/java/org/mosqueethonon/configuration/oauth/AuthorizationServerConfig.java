package org.mosqueethonon.configuration.oauth;

import org.mosqueethonon.configuration.security.ProfileProvider;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurRoleEntity;
import org.mosqueethonon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;
import java.util.UUID;

import static org.mosqueethonon.configuration.security.ProfileProvider.*;

@Configuration
public class AuthorizationServerConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileProvider profileProvider;


    @Bean
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());
        return http.formLogin(Customizer.withDefaults()).cors(cors -> cors.configurationSource(corsConfigurationSource)).build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        // Configuration d'un client OAuth enregistré
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("moth-react-app")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(getRedirectURI())
                .scope("ALL")
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(getIssuerURI())
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> accessTokenCustomizer() {
        return (context) -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                UtilisateurEntity user = (UtilisateurEntity) userService.loadUserByUsername(context.getPrincipal().getName());
                List<UtilisateurRoleEntity> roles = user.getRoles();
                if (!CollectionUtils.isEmpty(roles)) {
                    List<String> rolesList = roles.stream().map(UtilisateurRoleEntity::getRole).toList();
                    context.getClaims().claim("roles", rolesList);
                }
            }
        };
    }

    private String getIssuerURI() {
        String activeProfile = profileProvider.getActiveProfile();
        return switch (activeProfile) {
            case DEVELOPMENT -> "http://localhost:3000";
            case PRODUCTION -> "https://www.inscription-amc.fr";
            case STAGING -> "https://www.staging.inscription-amc.fr";
            default -> throw new IllegalArgumentException("Le profile '" + activeProfile + "' n'est pas géré !");
        };
    }

    private String getRedirectURI() {
        String activeProfile = profileProvider.getActiveProfile();
        return switch (activeProfile) {
            case DEVELOPMENT -> "http://localhost:3000/admin";
            case PRODUCTION -> "https://www.inscription-amc.fr/admin";
            case STAGING -> "https://www.staging.inscription-amc.fr/admin";
            default -> throw new IllegalArgumentException("Le profile '" + activeProfile + "' n'est pas géré !");
        };
    }

}
