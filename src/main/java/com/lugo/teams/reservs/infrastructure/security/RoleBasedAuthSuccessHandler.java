package com.lugo.teams.reservs.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
@Slf4j
public class RoleBasedAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // DEBUG: muestra las autoridades para verificar cómo se guardaron
        authorities.forEach(a -> log.info("Authority del usuario: {}", a.getAuthority()));

        // Usa el contextPath para generar redirects correctos cuando la app corre con context-path
        String base = request.getContextPath();
        String redirectUrl = base + "/";

        if (hasRole(authorities, "ROLE_SUPERADMIN")) {
            redirectUrl = base + "/admin/owners";
        } else if (hasRole(authorities, "ROLE_OWNER")) {
            redirectUrl = base + "/dashboard/owner";
        } else if (hasRole(authorities, "ROLE_USER")) {
            redirectUrl = base + "/dashboard/user";
        }

        log.info("Login exitoso ({}). Redirect a {}", authentication.getName(), redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String role) {
        if (authorities == null) return false;
        return authorities.stream()
                .anyMatch(a -> role.equals(a.getAuthority()));
    }
}
