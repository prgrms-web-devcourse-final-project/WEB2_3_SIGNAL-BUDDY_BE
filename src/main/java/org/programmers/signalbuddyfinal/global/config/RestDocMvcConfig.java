package org.programmers.signalbuddyfinal.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RestDocMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/docs/**") // 해당 경로만 요청 가능
                .addResourceLocations("classpath:/static/docs/");   // 해당 경로에 있는 리소스만 매핑
    }
}
