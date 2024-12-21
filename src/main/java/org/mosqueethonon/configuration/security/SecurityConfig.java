package org.mosqueethonon.configuration.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.mosqueethonon.configuration.security.ProfileProvider.*;
import static org.mosqueethonon.configuration.security.ProfileProvider.STAGING;

@EnableWebSecurity
@AllArgsConstructor
@Configuration
public class SecurityConfig {

    @Autowired
    private ProfileProvider profileProvider;

    private static final String[] AUTH_WHITE_LIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**"
    };

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.POST, "/v1/inscriptions-enfants/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/inscriptions-adultes/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/adhesions").permitAll()
                .requestMatchers("/v1/tarifs").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/tarifs-inscription/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/v1/params/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/v1/classes/**").hasRole("ENSEIGNANT")
                .requestMatchers(HttpMethod.POST,"/v1/classes/**/presences").hasRole("ENSEIGNANT")
                .requestMatchers(HttpMethod.PUT,"/v1/classes/**/presences").hasRole("ENSEIGNANT")
                .requestMatchers("/v1/presences/**").hasRole("ENSEIGNANT")
                .requestMatchers("/v1/eleves/**/**").hasRole("ENSEIGNANT")
                .requestMatchers(HttpMethod.OPTIONS, "/v1/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/login").permitAll()
                .requestMatchers(HttpMethod.GET,"/logout").permitAll()
                .requestMatchers(AUTH_WHITE_LIST).permitAll()
                .anyRequest().hasRole("ADMIN"))
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .formLogin(login -> login.loginPage("/login").permitAll())
                .logout(logout -> logout.clearAuthentication(true).invalidateHttpSession(true).deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .permitAll());
        return http.build();
    }

    private LogoutSuccessHandler logoutSuccessHandler() {
        SimpleUrlLogoutSuccessHandler handler = new SimpleUrlLogoutSuccessHandler();
        handler.setDefaultTargetUrl(getRedirectURIAfterLogout());  // Rediriger vers la page d'accueil après déconnexion
        return handler;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailService){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl r = new RoleHierarchyImpl();
        r.setHierarchy("ROLE_ADMIN > ROLE_ENSEIGNANT");
        return r;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter2 = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter2.setAuthoritiesClaimName("roles");
        jwtGrantedAuthoritiesConverter2.setAuthorityPrefix(StringUtils.EMPTY);
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter2);
        return jwtAuthenticationConverter;
    }

    /**
     * CORS configuration pour les ressources OAuth
     * @return la config cors des ressources oauth
     */
    @Bean
    @Qualifier("corsConfigurationSource")
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(getAllowedOrigins()));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private String[] getAllowedOrigins() {
        String activeProfile = profileProvider.getActiveProfile();
        return switch (activeProfile) {
            case DEVELOPMENT, TEST -> new String[]{"http://localhost:3000"};
            case PRODUCTION -> new String[]{"https://www.inscription-amc.fr", "https://inscription-amc.fr"};
            case STAGING ->
                    new String[]{"https://www.staging.inscription-amc.fr", "https://staging.inscription-amc.fr"};
            default -> throw new IllegalArgumentException("Le profile '" + activeProfile + "' n'est pas géré !");
        };
    }

    @Bean
    public HttpFirewall allowSemicolonHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowSemicolon(true); // Permettre le point-virgule
        return firewall;
    }

    private String getRedirectURIAfterLogout() {
        String activeProfile = profileProvider.getActiveProfile();
        return switch (activeProfile) {
            case DEVELOPMENT, TEST -> "http://localhost:3000";
            case PRODUCTION -> "https://www.inscription-amc.fr";
            case STAGING -> "https://www.staging.inscription-amc.fr";
            default -> throw new IllegalArgumentException("Le profile '" + activeProfile + "' n'est pas géré !");
        };
    }

    /*@Bean
    public FilterRegistrationBean<RequestLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new RequestLoggingFilter());
        registrationBean.addUrlPatterns("/*"); // Appliquer le filtre à toutes les URL

        return registrationBean;
    }*/
}
