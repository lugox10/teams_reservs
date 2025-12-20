package com.lugo.teams.reservs.infrastructure.security;

import com.lugo.teams.reservs.domain.model.ReservUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;

    public UserDetailsImpl(ReservUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.enabled = user.isEnabled();

        // Evita duplicar el prefijo ROLE_ si el enum ya incluye ROLE_
        String roleName = (user.getRole() != null) ? user.getRole().name() : "USER";
        String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(authority));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // mantenemos el resto igual
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !userIsLocked();
    }

    private boolean userIsLocked() {
        return !enabled; // o usa otro campo si manejas locking separado
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // Mantén tu generateToken si lo usas; aquí es placeholder
    public String generateToken() {
        return "token-placeholder";
    }
}
