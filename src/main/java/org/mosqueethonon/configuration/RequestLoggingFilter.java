package org.mosqueethonon.configuration;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Classe utilisée pour investiguer des problèmes de sécurité CORS (log des requêtes HTTP entrantes)
 */
public class RequestLoggingFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Ne rien faire lors de l'initialisation
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        // Journalisation des informations sur la requête
        logger.info("Request URL: {}", httpRequest.getRequestURL());
        logger.info("Method: {}", httpRequest.getMethod());
        logger.info("Headers:");
        Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            logger.info("{}: {}", headerName, httpRequest.getHeader(headerName));
        }

        // Continuer le traitement de la requête
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        // Ne rien faire lors de la destruction
    }

}
