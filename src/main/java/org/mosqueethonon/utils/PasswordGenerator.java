package org.mosqueethonon.utils;

import java.security.SecureRandom;

public class PasswordGenerator {

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_+=<>?";

    // Combinaison de tous les caractères
    private static final String PASSWORD_ALLOW = CHAR_LOWER + CHAR_UPPER + DIGITS + SYMBOLS;

    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("La longueur minimale recommandée est de 8 caractères");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int rndCharIndex = random.nextInt(PASSWORD_ALLOW.length());
            sb.append(PASSWORD_ALLOW.charAt(rndCharIndex));
        }
        return sb.toString();
    }

}
