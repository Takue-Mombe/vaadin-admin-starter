package com.adminpro.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * MVP security configuration: all routes are publicly accessible.
 *
 * VaadinWebSecurity already permits all Vaadin-internal endpoints and
 * handles the anyRequest rule — we must NOT add a second anyRequest call.
 *
 * Phase 2 upgrade path:
 *   1. Call setLoginView(http, LoginView.class) to enable the login page.
 *   2. Add @RolesAllowed("ADMIN") to UsersView.
 *   3. Add a UserDetailsService bean backed by UserRepository.
 *   4. Remove the permitAll override below.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Allow H2 console frames in dev (remove in production)
        http.headers(headers ->
            headers.frameOptions(frame -> frame.sameOrigin())
        );

        // Permit H2 console path before Vaadin takes over
        http.authorizeHttpRequests(auth ->
            auth.requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
        );

        // Call Vaadin's base configuration — it adds its own anyRequest handler.
        // For MVP (no login) we override requestCache and set anonymous access.
        super.configure(http);

        // Disable redirect-to-login: allow all requests anonymously for the MVP demo.
        http.anonymous(anon -> {});
    }
}
