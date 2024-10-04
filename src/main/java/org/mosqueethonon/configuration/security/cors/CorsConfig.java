package org.mosqueethonon.configuration.security.cors;

import static org.mosqueethonon.configuration.security.ProfileProvider.*;

import org.mosqueethonon.configuration.security.ProfileProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Autowired
    private ProfileProvider profileProvider;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(getAllowedOrigins())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }

    /**
     * CORS configuration pour les ressources OAuth
     * @return la config cors des ressources oauth
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(getAllowedOrigins()));
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/oauth2/**", configuration);
        return source;
    }

    private String[] getAllowedOrigins() {
        String activeProfile = profileProvider.getActiveProfile();
        return switch (activeProfile) {
            case DEVELOPMENT -> new String[]{"http://localhost:3000"};
            case PRODUCTION -> new String[]{"https://www.inscription-amc.fr", "https://inscription-amc.fr"};
            case STAGING ->
                    new String[]{"https://www.staging.inscription-amc.fr", "https://staging.inscription-amc.fr"};
            default -> throw new IllegalArgumentException("Le profile '" + activeProfile + "' n'est pas géré !");
        };
    }

}
