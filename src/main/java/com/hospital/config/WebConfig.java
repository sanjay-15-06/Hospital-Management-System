package com.hospital.config;

import com.hospital.controller.SessionAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final SessionAccessInterceptor accessInterceptor;
    public WebConfig(SessionAccessInterceptor accessInterceptor) { this.accessInterceptor = accessInterceptor; }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessInterceptor);
    }
}
