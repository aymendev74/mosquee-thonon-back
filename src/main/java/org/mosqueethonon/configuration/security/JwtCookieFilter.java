package org.mosqueethonon.configuration.security;

import com.google.common.collect.Lists;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.mosqueethonon.service.auth.IAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@AllArgsConstructor
public class JwtCookieFilter extends OncePerRequestFilter {

    private static final List<String> AUTHENTICATION_ENDPOINTS = Lists.newArrayList("/login", "/token", "/authorize");

    private IAuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String token = extractTokenFromCookie(request);
        // Si le token a expiré on le supprime des cookies renvoyés dans la réponse
        if(token != null) {
            this.authService.deleteTokenIfExpired(response, token);
        }

        // Et on ajoute le header Authorization dans la requête en cours de traitement pour spring security
        if (request.getHeader(HttpHeaders.AUTHORIZATION) == null && AUTHENTICATION_ENDPOINTS.stream().noneMatch(path::endsWith)) {
            if (token != null) {
                HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
                    @Override
                    public String getHeader(String name) {
                        if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                            return "Bearer " + token;
                        }
                        return super.getHeader(name);
                    }
                };
                filterChain.doFilter(wrapped, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("MOTH-TOKEN".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

}
