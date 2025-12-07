package org.mosqueethonon.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class UserActivationTokenGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64UrlEncoder = Base64.getUrlEncoder().withoutPadding();

    /**
     * Génère un token sécurisé pour l'activation d'un user, URL safe
     * @param lengthBytes longueur du token en bytes
     * @return token encodé en Base64 URL-safe
     */
    public static String generateToken(int lengthBytes) {
        byte[] randomBytes = new byte[lengthBytes];
        secureRandom.nextBytes(randomBytes);
        return base64UrlEncoder.encodeToString(randomBytes);
    }

}
