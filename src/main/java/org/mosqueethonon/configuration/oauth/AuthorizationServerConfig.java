package org.mosqueethonon.configuration.oauth;

import org.mosqueethonon.configuration.security.ApplicationProperties;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.entity.utilisateur.UtilisateurRoleEntity;
import org.mosqueethonon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.CollectionUtils;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Configuration
public class AuthorizationServerConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Value("${server.port}")
    private String serverPort;


    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http, @Qualifier("corsConfigurationSource") CorsConfigurationSource corsSource) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
        return http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
             .with(authorizationServerConfigurer, (authorizationServer) ->
                    authorizationServer.oidc(Customizer.withDefaults()))
             .authorizeHttpRequests((authorize) ->
                        authorize.anyRequest().authenticated())
             .cors(cors -> cors.configurationSource(corsSource))
             .formLogin(Customizer.withDefaults())
                .logout(Customizer.withDefaults())
                .build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("moth-react-app")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(this.applicationProperties.getLoginRedirectUri())
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                .tokenSettings(tokenSettings())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer(this.applicationProperties.getIssuerUri())
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

    @Bean
    public TokenSettings tokenSettings() {
        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(1))
                .build();
    }

    /**
     * Sert à décoder le token reçue dans les entêtes HTTP
     * @return
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(this.applicationProperties.getJwtDecoderUri()).build();
    }

}
