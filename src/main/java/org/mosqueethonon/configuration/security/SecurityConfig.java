package org.mosqueethonon.configuration.security;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.stream.Collectors;

@EnableWebSecurity
@AllArgsConstructor
@Configuration
public class SecurityConfig {

    private static final String[] AUTH_WHITE_LIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/v1/user/auth").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/inscriptions-enfants/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/inscriptions-adultes/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/adhesions").permitAll()
                .requestMatchers("/v1/tarifs").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/tarifs-inscription/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/v1/params/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/v1/**").permitAll()
                .requestMatchers(AUTH_WHITE_LIST).permitAll()
                .anyRequest().hasRole("ADMIN"))
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .formLogin(Customizer.withDefaults());
        return http.build();
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

    /*@Bean
    public FilterRegistrationBean<RequestLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new RequestLoggingFilter());
        registrationBean.addUrlPatterns("/*"); // Appliquer le filtre à toutes les URL

        return registrationBean;
    }*/
}
