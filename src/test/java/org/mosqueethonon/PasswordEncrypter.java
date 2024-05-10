package org.mosqueethonon;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncrypter {

    @Test
    public void encryptPassword() {
        String encodedPassword = new BCryptPasswordEncoder().encode("123456");
        System.out.println(encodedPassword);
    }

}
