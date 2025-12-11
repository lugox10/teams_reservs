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
public class SecurityConfig {

    // NOTA: no inyectamos RoleBasedAuthSuccessHandler por constructor para evitar ciclos.
    // Lo recibimos como parámetro del método @Bean securityFilterChain(...).

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   RoleBasedAuthSuccessHandler authSuccessHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/teams-reservs/login",
                                "/css/**", "/js/**", "/images/**",
                                "/reserv-users/**", "/reserv-users/register",
                                "/login", "/error"
                        ).permitAll()
                        .requestMatchers("/owner/**").hasRole("OWNER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/teams-reservs/login")
                        .loginProcessingUrl("/teams-reservs/login")
                        .usernameParameter("login")
                        .passwordParameter("password")
                        .successHandler(authSuccessHandler)
                        .failureUrl("/teams-reservs/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/teams-reservs/logout")
                        .logoutSuccessUrl("/teams-reservs/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/webhook/**")));

        return http.build();
    }

    // Hacemos el bean estático para que se cree sin instanciar la clase de configuración,
    // evitando que otros beans que dependan de SecurityConfig creen ciclos.
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
