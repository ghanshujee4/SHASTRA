package com.library.sdl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // No CORS configuration here!
    // All CORS settings are handled in SecurityConfig to avoid conflicts.
}
