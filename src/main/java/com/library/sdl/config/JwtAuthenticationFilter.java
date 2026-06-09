package com.library.sdl.config;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ 🚨 VERY IMPORTANT — skip WebSocket requests
        if (path.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        log.info("🔐 Authorization header = {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            log.debug("No Authorization header (public or unauthenticated request)");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username;

        try {
            username = jwtService.extractUsername(token);
            log.info("✅ Username from token = {}", username);
//        } catch (Exception e) {
//            log.error("❌ JWT parsing failed", e);
//            filterChain.doFilter(request, response);
//            return;
//        }
        } catch (ExpiredJwtException e) {
            log.warn("❌ JWT expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"JWT expired\"}");
            return; // 🔴 STOP FILTER CHAIN
        } catch (Exception e) {
            log.error("❌ JWT parsing failed", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return; // 🔴 STOP FILTER CHAIN
        }


        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
try{
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            log.info("👤 Loaded user = {}", userDetails.getUsername());

            if (jwtService.isTokenValid(token, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("✅ SecurityContext authentication SET");
                log.info("Authorities = {}", userDetails.getAuthorities());
            } else {
                log.warn("❌ Token invalid");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
} catch (UsernameNotFoundException ex) {
    log.warn("⚠️ User not found when loading by username: {}. Trying token role fallback.", username);

    // If the token represents an admin user that doesn't exist in DB (e.g. a hardcoded admin),
    // allow authentication based on the role claim inside the token.
    try {
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        log.info("Role from token (fallback) = {}", role);
        if (role != null && "ADMIN".equalsIgnoreCase(role)) {
            // create a simple UserDetails with ADMIN authority
            UserDetails adminDetails = new org.springframework.security.core.userdetails.User(
                    username,
                    "", // no password needed here
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );

            if (jwtService.isTokenValid(token, adminDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                adminDetails,
                                null,
                                adminDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("✅ SecurityContext authentication SET for admin fallback");
            } else {
                log.warn("❌ Token invalid for admin fallback");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return; // STOP filter chain
        }
    } catch (Exception e) {
        log.warn("Failed to apply admin fallback: {}", e.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }
}
        }

        filterChain.doFilter(request, response);
    }
}