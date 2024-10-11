package org.mosqueethonon.configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ProfileProvider {

    public static final String DEVELOPMENT = "dev";
    public static final String PRODUCTION = "prod";
    public static final String STAGING = "sta";
    public static final String TEST = "test";

    @Autowired
    private Environment environment;

    public String getActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if(activeProfiles.length != 1) {
            throw new IllegalStateException("Il ne peut y avoir plus de 1 profile actif !");
        }
        return activeProfiles[0];
    }
}
