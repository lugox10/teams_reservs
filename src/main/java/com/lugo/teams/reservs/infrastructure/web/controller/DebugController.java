package com.lugo.teams.reservs.infrastructure.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/teams-reservs")
public class DebugController {

    /**
     * Endpoint de diagnóstico para usar después de hacer login.
     * Ejemplo: GET /teams-reservs/debug/whoami
     */
    @GetMapping("/debug/whoami")
    public Map<String, Object> whoami(Principal principal, Authentication auth) {
        Map<String, Object> out = new HashMap<>();
        out.put("principal", principal == null ? null : principal.getName());
        if (auth != null) {
            out.put("authenticated", auth.isAuthenticated());
            out.put("authorities", auth.getAuthorities());
        } else {
            out.put("authenticated", false);
            out.put("authorities", null);
        }
        return out;
    }
}
