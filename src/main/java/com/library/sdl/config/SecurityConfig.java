package com.library.sdl.config;

import com.library.sdl.idCard.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userDetailsService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }
@Bean
public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(userDetailsService);

    provider.setPasswordEncoder(passwordEncoder());

    return provider;
}

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users",
                                "/api/users/login",
                                "/api/users/register",
                                "/api/users/admin/login",
//                                "/api/payments",
                                // "/api/payments/**",
                                "/uploads/**",
                                "/static/uploads/**",
                                "/ws/**",          // ✅ allow websocket noise
                                "/error",
                                "/api/shifts",
                                "/api/sheat",
                                "/api/auth/**",
                                "/api/seats/**",
                                // "/api/users/**",
                                "/api/users/check-email",
                                "/api/users/check-mobile",
                                "/api/email/sendBulk",
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/docs/**",
                                "/api/chat/health",
                                "/chat.html"
                        ).permitAll()
                        // 🤖 AI chat (Llama via Ollama)
                        .requestMatchers("/api/chat/admin").hasRole("ADMIN")
                        .requestMatchers("/api/chat/student").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/chat").hasAnyRole("USER", "ADMIN")
                        // 👑 ADMIN → ACCESS EVERYTHING
                        .requestMatchers(
                                "/api/admin/**", "/topic/notifications/**").hasRole("ADMIN")
                        // 👤 USER → LIMITED ACCESS
                        .requestMatchers(
                                //"/api/users/**",
                                "/api/idcard/**",
                                "/api/payments/**",
                                "/api/requests/**",
                                "/api/users/**",
                                "/api/idcard/card-data",
                                "/api/payments/overdue"

                        ).hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated() // 🔐 includes /api/idcard/**
                )


                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://manage.shastradigitallibrary.com","http://145.223.21.103"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
