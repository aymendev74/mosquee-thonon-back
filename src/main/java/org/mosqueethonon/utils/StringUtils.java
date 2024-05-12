package org.mosqueethonon.utils;

public class StringUtils {

    public static String normalize(String input) {
        if(input == null) {
            return null;
        }
        // Suppression des espaces au début et à la fin de la chaîne
        String trimmedString = input.trim();

        // Division de la chaîne en mots séparés par des espaces
        String[] words = trimmedString.split("\\s+");

        // Création d'une chaîne pour stocker le résultat normalisé
        StringBuilder normalizedString = new StringBuilder();

        // Parcours de chaque mot dans la chaîne
        for (String word : words) {
            // Si le mot n'est pas vide
            if (!word.isEmpty()) {
                // Mise en majuscule de la première lettre du mot et concaténation avec le reste du mot
                String normalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
                // Ajout du mot normalisé à la chaîne résultat
                normalizedString.append(normalizedWord).append(" ");
            }
        }

        return normalizedString.toString().trim();
    }
}
