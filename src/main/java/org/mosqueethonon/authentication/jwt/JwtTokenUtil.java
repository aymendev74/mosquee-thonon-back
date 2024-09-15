package org.mosqueethonon.authentication.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.mosqueethonon.entity.utilisateur.UtilisateurRoleEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.crypto.SecretKey;

@Component
public class JwtTokenUtil {

    private static final long EXPIRE_DURATION = 24 * 60 * 60 * 1000; // 24 hour

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${app.jwt.secret}")
    private String SECRET_KEY;

    public String generateAccessToken(UtilisateurEntity user) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        List<UtilisateurRoleEntity> roles = user.getRoles();
        List<String> rolesList = null;
        if(!CollectionUtils.isEmpty(roles)) {
            rolesList = roles.stream().map(UtilisateurRoleEntity::getRole).toList();
        }
        return Jwts.builder()
                .subject(String.format("%s,%s", user.getId(), user.getUsername()))
                .issuer("MosqueeThonon")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_DURATION))
                .signWith(key, Jwts.SIG.HS512)
                .claim("roles", rolesList)
                .compact();
    }


    public boolean validateAccessToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException ex) {
            LOGGER.error(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            LOGGER.error(ex.getMessage());
        }

        return false;
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
                .getPayload();
    }

}
