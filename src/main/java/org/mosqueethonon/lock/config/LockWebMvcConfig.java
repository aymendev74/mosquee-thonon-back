package org.mosqueethonon.lock.config;

import lombok.RequiredArgsConstructor;
import org.mosqueethonon.lock.interceptor.LockInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class LockWebMvcConfig implements WebMvcConfigurer {

    private final LockInterceptor lockInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(lockInterceptor);
    }

}
