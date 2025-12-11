package com.lugo.teams.reservs.infrastructure.security;

import com.lugo.teams.reservs.application.dto.user.ReservUserResponseDTO;
import com.lugo.teams.reservs.application.service.ReservUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class RoleBasedAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final ReservUserService reservUserService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String username = authentication.getName(); // value from UserDetails.username

        boolean isOwner = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
        boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isOwner || isAdmin) {
            // intentamos resolver ownerId numérico
            Optional<ReservUserResponseDTO> uOpt = reservUserService.findByUsername(username);
            String ownerParam = uOpt.map(u -> String.valueOf(u.getId())).orElse(username);
            response.sendRedirect(request.getContextPath() + "/dashboard/owner?ownerId=" + ownerParam);
            return;
        }

        // user normal
        response.sendRedirect(request.getContextPath() + "/dashboard/user");
    }
}
