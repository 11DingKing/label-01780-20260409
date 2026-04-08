package com.help.mp.config;

import com.help.mp.interceptor.AdminAuthInterceptor;
import com.help.mp.interceptor.MpAuthInterceptor;
import com.help.mp.interceptor.PerformanceInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final MpAuthInterceptor mpAuthInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;
    private final PerformanceInterceptor performanceInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 性能监控拦截器（所有API）
        registry.addInterceptor(performanceInterceptor)
                .addPathPatterns("/api/**");
        registry.addInterceptor(mpAuthInterceptor)
                .addPathPatterns("/api/mp/**")
                .excludePathPatterns("/api/mp/auth/login", "/api/mp/tip/notify");
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/auth/login");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**").addResourceLocations("file:" + Paths.get("upload").toAbsolutePath() + "/");
    }
}
