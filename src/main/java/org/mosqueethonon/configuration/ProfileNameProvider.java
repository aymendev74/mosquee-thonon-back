package org.mosqueethonon.configuration;

import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProfileNameProvider {

    private Environment environment;

    private String getActiveProfileName() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return activeProfiles[0];
        }
        return "No active profile found";
    }

    public boolean isProdOrDev() {
        String activeProfile = getActiveProfileName();
        return "prod".equals(activeProfile) || "dev".equals(activeProfile);
    }

}
