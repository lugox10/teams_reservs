package com.lugo.teams.reservs.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RoleBasedAuthSuccessHandler authSuccessHandler
    ) throws Exception {

        http
                // 🔐 AUTORIZACIÓN
                .authorizeHttpRequests(auth -> auth

                        // Públicas
                        .requestMatchers(
                                "/teams-reservs/login",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/reserv-users/**",
                                "/error"
                        ).permitAll()

                        // OWNER
                        .requestMatchers("/dashboard/owner/**").hasRole("OWNER")

                        // USER
                        .requestMatchers(
                                "/dashboard/user/**",
                                "/venues/**",
                                "/reservations/**"
                        ).hasRole("USER")

                        // Cualquier otra → autenticado
                        .anyRequest().authenticated()
                )

                // 🔑 LOGIN
                .formLogin(form -> form
                        .loginPage("/teams-reservs/login")
                        .loginProcessingUrl("/teams-reservs/login")
                        .usernameParameter("login")
                        .passwordParameter("password")
                        .successHandler(authSuccessHandler)
                        .failureUrl("/teams-reservs/login?error=true")
                        .permitAll()
                )

                // 🚪 LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/teams-reservs/logout")
                        .logoutSuccessUrl("/teams-reservs/login?logout")
                        .permitAll()
                )

                // 🛡️ CSRF (webhooks / integraciones)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/webhook/**")
                        )
                );

        return http.build();
    }

    // 🔐 Password encoder
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔐 Authentication manager
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
