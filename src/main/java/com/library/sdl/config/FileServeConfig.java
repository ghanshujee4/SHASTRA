package com.library.sdl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileServeConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve files under "uploads/" as static content
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/"); // relative to project root
    }
}

