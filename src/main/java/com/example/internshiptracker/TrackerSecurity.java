package com.example.internshiptracker;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class TrackerSecurity {
    @Bean @Profile("!private")
    SecurityFilterChain localSecurity(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a -> a.anyRequest().permitAll()).csrf(c -> c.disable()).build();
    }

    @Bean @Profile("private")
    InMemoryUserDetailsManager users(@Value("${TRACKER_PASSWORD_HASH}") String hash) {
        if (!hash.startsWith("{bcrypt}")) throw new IllegalArgumentException("BCrypt password hash required");
        return new InMemoryUserDetailsManager(User.withUsername("owner").password(hash).roles("OWNER").build());
    }

    @Bean @Profile("private") @Order(1)
    SecurityFilterChain automation(HttpSecurity http, @Value("${MUSE_TOKEN_SHA256}") String digest) throws Exception {
        byte[] expected = HexFormat.of().parseHex(digest);
        if (expected.length != 32) throw new IllegalArgumentException("SHA-256 token hash required");
        var tokenFilter = new OncePerRequestFilter() {
            @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
                String header = req.getHeader("Authorization");
                if (header != null && header.startsWith("Bearer ")) {
                    try {
                        byte[] actual = MessageDigest.getInstance("SHA-256").digest(header.substring(7).getBytes(StandardCharsets.UTF_8));
                        if (MessageDigest.isEqual(expected, actual)) {
                            var context = SecurityContextHolder.createEmptyContext();
                            context.setAuthentication(new UsernamePasswordAuthenticationToken("muse", null, List.of(new SimpleGrantedAuthority("ROLE_MUSE"))));
                            SecurityContextHolder.setContext(context);
                        }
                    } catch (NoSuchAlgorithmException e) { throw new ServletException(e); }
                }
                chain.doFilter(req, res);
            }
        };
        return http.securityMatcher(r -> r.getRequestURI().startsWith("/api/") && r.getHeader("Authorization") != null)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(c -> c.disable())
            .authorizeHttpRequests(a -> a.requestMatchers(HttpMethod.DELETE, "/api/**").denyAll().anyRequest().hasRole("MUSE"))
            .exceptionHandling(e -> e.authenticationEntryPoint((req,res,ex) -> res.sendError(401)))
            .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class).build();
    }

    @Bean @Profile("private") @Order(2)
    SecurityFilterChain owner(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(a -> a.requestMatchers("/healthz", "/error").permitAll().anyRequest().hasRole("OWNER"))
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(l -> l.logoutSuccessUrl("/login?logout"))
            .exceptionHandling(e -> e.authenticationEntryPoint((req,res,ex) -> { if (req.getRequestURI().startsWith("/api/")) res.sendError(401); else res.sendRedirect("/login"); }))
            .build();
    }
}


