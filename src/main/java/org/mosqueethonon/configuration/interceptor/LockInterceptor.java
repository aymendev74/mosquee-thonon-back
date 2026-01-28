package org.mosqueethonon.configuration.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.annotations.RequireLock;
import org.mosqueethonon.enums.ResourceTypeEnum;
import org.mosqueethonon.service.lock.LockService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockInterceptor implements HandlerInterceptor {

    private final LockService lockService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireLock requireLock = handlerMethod.getMethodAnnotation(RequireLock.class);

        if (requireLock == null) {
            return true;
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        ResourceTypeEnum resourceType = requireLock.resourceType();
        String resourceIdParam = requireLock.resourceIdParam();

        @SuppressWarnings("unchecked")
        Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        
        String resourceIdStr = pathVariables != null ? pathVariables.get(resourceIdParam) : null;
        
        if (resourceIdStr == null) {
            resourceIdStr = request.getParameter(resourceIdParam);
        }

        if (resourceIdStr == null) {
            log.error("Resource ID parameter '{}' not found in request", resourceIdParam);
            throw new IllegalArgumentException("Paramètre de ressource manquant: " + resourceIdParam);
        }

        Long resourceId = Long.parseLong(resourceIdStr);

        lockService.verifyLock(resourceType, resourceId, username);

        return true;
    }

}
