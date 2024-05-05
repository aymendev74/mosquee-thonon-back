package org.mosqueethonon.configuration;

import lombok.AllArgsConstructor;
import org.mosqueethonon.authentication.jwt.JwtTokenFilter;
import org.mosqueethonon.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@AllArgsConstructor
@Configuration
public class SecurityConfig {

    private JwtTokenFilter jwtTokenFilter;

    private UserService userService;

    private static final String[] AUTH_WHITE_LIST = {
            "/v3/api-docs/**",
            "/v2/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(authorize -> authorize
                /*.requestMatchers("/api/v1/user/auth").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/inscriptions").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/adhesions").permitAll()
                .requestMatchers("/api/v1/periodes").permitAll()
                .requestMatchers("/api/v1/tarifs").permitAll()
                .requestMatchers("/api/v1/tarifs-inscription").permitAll()
                .requestMatchers("api/v1/params/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/v1/**").permitAll()
                .requestMatchers(AUTH_WHITE_LIST).permitAll()*/
                .anyRequest().authenticated());

        /* http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.exceptionHandling()
                .authenticationEntryPoint(
                        (request, response, ex) -> {
                            response.sendError(
                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    ex.getMessage()
                            );
                        }
                );*/

        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailService, PasswordEncoder passEncoder){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(passEncoder);
        return new ProviderManager(authProvider);
    }
}
