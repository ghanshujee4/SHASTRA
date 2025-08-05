package com.library.sdl.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import javax.imageio.ImageIO;

@Configuration
public class ImageIOConfig {

    @PostConstruct
    public void registerImageIOPlugins() {
        ImageIO.scanForPlugins();  // This loads additional image readers (e.g. from TwelveMonkeys)
        System.out.println("ImageIO plugins registered");
    }
}

