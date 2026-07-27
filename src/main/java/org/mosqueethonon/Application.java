package org.mosqueethonon;

import org.mosqueethonon.common.config.TimeConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        // Doit rester avant le démarrage de Spring : le driver PostgreSQL dérive le fuseau de la
        // session du fuseau par défaut de la JVM. Le fuseau lui-même est déclaré dans TimeConfiguration.
        TimeZone.setDefault(TimeZone.getTimeZone(TimeConfiguration.ZONE_APPLICATION));
        SpringApplication.run(Application.class, args);
    }
}
