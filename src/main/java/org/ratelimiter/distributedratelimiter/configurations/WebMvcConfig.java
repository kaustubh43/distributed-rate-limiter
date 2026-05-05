package org.ratelimiter.distributedratelimiter.configurations;

import lombok.RequiredArgsConstructor;
import org.ratelimiter.distributedratelimiter.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                // We typically only want to rate-limit actual API calls.
                // We don't want to limit static resources (HTML/CSS) or health checks.
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health");
    }
}