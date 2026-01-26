package org.mosqueethonon.configuration;

import lombok.RequiredArgsConstructor;
import org.mosqueethonon.configuration.interceptor.LockInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LockInterceptor lockInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(lockInterceptor);
    }

}
