package org.mosqueethonon.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Point de déclaration unique du fuseau horaire de l'application.
 *
 * <p>L'association gère des inscriptions, des périodes et des tarifs qui basculent à des dates
 * précises : toute la logique temporelle doit donc raisonner en heure locale française, quel que
 * soit le fuseau de la machine hôte.
 *
 * <p>Deux usages complémentaires :
 * <ul>
 *   <li>le bean {@link Clock} est injecté dans les services et remplace les appels à
 *       {@code LocalDate.now()} / {@code LocalDateTime.now()} sans argument, dont le résultat
 *       dépendait implicitement du fuseau par défaut de la JVM. Il rend aussi le temps
 *       contrôlable dans les tests, via {@link Clock#fixed} ;</li>
 *   <li>{@link #ZONE_APPLICATION} sert aux rares contextes où l'injection n'est pas possible
 *       (méthodes statiques, listeners JPA) et au positionnement du fuseau par défaut de la JVM
 *       au démarrage.</li>
 * </ul>
 *
 * <p>Le fuseau par défaut de la JVM reste positionné dans {@code Application.main()} : il doit
 * l'être avant toute autre initialisation (le driver PostgreSQL en dérive le fuseau de session,
 * qui détermine l'évaluation de {@code CURRENT_DATE} côté base), donc avant que la configuration
 * Spring ne soit chargée.
 */
@Configuration
public class TimeConfiguration {

    public static final ZoneId ZONE_APPLICATION = ZoneId.of("Europe/Paris");

    @Bean
    public Clock clock() {
        return Clock.system(ZONE_APPLICATION);
    }

}
