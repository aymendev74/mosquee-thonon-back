package org.mosqueethonon.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Interrompt le démarrage lorsqu'une variable d'environnement référencée par la configuration de
 * l'application est absente.
 *
 * <p>Sans ce contrôle, l'absence passe inaperçue. Le binder des {@code @ConfigurationProperties}
 * ignore les placeholders qu'il ne sait pas résoudre et conserve le texte brut : une propriété de
 * type {@code String} reçoit littéralement {@code "${MA_VARIABLE}"}. Aucune validation
 * {@code @NotBlank} ne peut le détecter, puisque cette chaîne n'est ni nulle ni vide. L'application
 * démarre donc normalement et n'échoue qu'à la première utilisation de la propriété, loin de la
 * cause. Seules les propriétés typées ({@code Long}, {@code Boolean}...) échouent d'elles-mêmes, et
 * sur une erreur de conversion qui ne désigne pas la variable manquante.
 *
 * <p>Le contrôle a délibérément lieu sur {@link ApplicationPreparedEvent} : à ce stade, toutes les
 * sources de propriétés sont en place — y compris le fichier {@code .env} chargé par spring-dotenv,
 * qui s'enregistre via un {@code SpringApplicationRunListener} — et aucun bean n'a encore été
 * instancié.
 *
 * <p>Enregistré dans {@code META-INF/spring.factories} : un simple {@code @Component} ne
 * conviendrait pas, l'événement étant publié avant la création des beans du contexte.
 */
@Slf4j
public class RequiredEnvironmentVariablesValidator implements ApplicationListener<ApplicationPreparedEvent> {

    private static final String PLACEHOLDER_PREFIX = "${";

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        ConfigurableEnvironment environment = event.getApplicationContext().getEnvironment();
        Map<String, String> unresolved = findUnresolvedPlaceholders(environment);

        if (unresolved.isEmpty()) {
            log.debug("Toutes les variables d'environnement référencées par la configuration sont renseignées");
            return;
        }

        throw new IllegalStateException(buildErrorMessage(unresolved));
    }

    /**
     * Recense les propriétés dont la valeur contient un placeholder que l'environnement ne sait pas
     * résoudre, indexées par nom de propriété.
     */
    static Map<String, String> findUnresolvedPlaceholders(ConfigurableEnvironment environment) {
        Map<String, String> unresolved = new LinkedHashMap<>();
        Set<String> alreadySeen = new HashSet<>();

        for (PropertySource<?> source : environment.getPropertySources()) {
            if (!(source instanceof EnumerablePropertySource<?> enumerableSource)) {
                continue;
            }

            for (String propertyName : enumerableSource.getPropertyNames()) {
                // Les sources sont parcourues par priorité décroissante : la première occurrence est
                // celle qui gagne. Marquer toutes les sources, y compris les variables
                // d'environnement, évite de signaler une valeur d'application.yml qu'une source plus
                // prioritaire remplace déjà (application-test.yml, propriété système...).
                if (!alreadySeen.add(propertyName)) {
                    continue;
                }

                // Seuls les fichiers de configuration de l'application portent des placeholders à
                // résoudre ; les variables d'environnement et les propriétés système en sont la
                // source, pas la cible.
                if (!(source instanceof OriginTrackedMapPropertySource)) {
                    continue;
                }

                if (!(enumerableSource.getProperty(propertyName) instanceof String value)
                        || !value.contains(PLACEHOLDER_PREFIX)) {
                    continue;
                }

                try {
                    environment.resolveRequiredPlaceholders(value);
                } catch (IllegalArgumentException e) {
                    unresolved.put(propertyName, value);
                }
            }
        }

        return unresolved;
    }

    private String buildErrorMessage(Map<String, String> unresolved) {
        StringBuilder message = new StringBuilder("Démarrage interrompu : ")
                .append(unresolved.size())
                .append(unresolved.size() > 1 ? " propriétés référencent" : " propriété référence")
                .append(" une variable d'environnement absente.");

        unresolved.forEach((propertyName, value) ->
                message.append(System.lineSeparator()).append("  - ").append(propertyName)
                        .append(" attend ").append(value));

        message.append(System.lineSeparator())
                .append("Renseignez ces variables (fichier .env en local, variables d'environnement au déploiement).");

        return message.toString();
    }

}
