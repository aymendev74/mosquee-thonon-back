package org.mosqueethonon.configuration.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

/**
 * Utile en debug pour vérifier les headers dans la réponse HTTP
 */
@Component
public class ResponseHeaderLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ResponseHeaderLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        chain.doFilter(request, response);

        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResp = (HttpServletResponse) response;
            Collection<String> headerNames = httpResp.getHeaderNames();

            logger.debug("=== Response Headers ===");
            for (String headerName : headerNames) {
                Collection<String> values = httpResp.getHeaders(headerName);
                for (String value : values) {
                    logger.debug("{}: {}", headerName, value);
                }
            }
        }
    }
}