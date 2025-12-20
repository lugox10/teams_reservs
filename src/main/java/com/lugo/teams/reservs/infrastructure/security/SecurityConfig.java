package com.lugo.teams.reservs.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityConfig {

    private final RoleBasedAuthSuccessHandler authSuccessHandler;

    // Rutas públicas (web UI, assets y endpoints de autenticación)
    private static final String[] PUBLIC_MATCHERS = new String[] {
            "/teams-reservs/login",
            "/teams-reservs/api/auth/**",
            "/api/auth/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/reserv-users/**",
            "/error",
            "/teams-reservs/debug/**"
    };

    // Rutas accesibles para usuarios con rol USER
    private static final String[] USER_MATCHERS = new String[] {
            "/dashboard/user/**",
            "/venues/**",
            "/reservations/**"
    };

    // Rutas solo para owner
    private static final String[] OWNER_MATCHERS = new String[] {
            "/dashboard/owner/**"
    };

    // Endpoints relacionados con pagos / webhooks
    private static final String PAYMENT_CALLBACK_PATH = "/payments/callback";
    private static final String PAYMENT_ENDPOINTS = "/payments/**";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Autorizaciones
                .authorizeHttpRequests(auth -> auth
                        // públicas
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()

                        // webhook de pagos: permitido público (validar firma en la aplicación)
                        .requestMatchers(PAYMENT_CALLBACK_PATH).permitAll()

                        // OWNER
                        .requestMatchers(OWNER_MATCHERS).hasRole("OWNER")

                        // USER
                        .requestMatchers(USER_MATCHERS).hasRole("USER")

                        // resto autenticado
                        .anyRequest().authenticated()
                )

                // Form login (mantener tu flujo actual)
                .formLogin(form -> form
                        .loginPage("/teams-reservs/login")
                        .loginProcessingUrl("/teams-reservs/login")
                        .usernameParameter("login")
                        .passwordParameter("password")
                        .successHandler(authSuccessHandler)
                        .failureUrl("/teams-reservs/login?error=true")
                        .permitAll()
                )

                // Logout
                .logout(logout -> logout
                        .logoutUrl("/teams-reservs/logout")
                        .logoutSuccessUrl("/teams-reservs/login?logout")
                        .permitAll()
                )

                // CSRF: ignorar para APIs y webhooks de pago (POSTs desde gateways)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/teams-reservs/api/**"),
                                new AntPathRequestMatcher("/api/**"),
                                new AntPathRequestMatcher(PAYMENT_CALLBACK_PATH)
                        )
                );

        // Opcional: harden headers si lo necesitás
        // http.headers().frameOptions().sameOrigin();

        return http.build();
    }

    // Password encoder (static bean para evitar problemas con proxys)
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication manager (para inyección en servicios si hace falta)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
