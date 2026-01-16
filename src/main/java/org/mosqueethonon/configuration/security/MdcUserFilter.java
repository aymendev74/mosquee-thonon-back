package org.mosqueethonon.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class MdcUserFilter extends OncePerRequestFilter {

    private static final String MDC_USERNAME_KEY = "username";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                MDC.put(MDC_USERNAME_KEY, username);
            } else {
                MDC.put(MDC_USERNAME_KEY, "anonymous");
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_USERNAME_KEY);
        }
    }
}
