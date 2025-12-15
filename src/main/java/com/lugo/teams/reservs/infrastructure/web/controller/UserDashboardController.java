package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import com.lugo.teams.reservs.application.dto.user.ReservUserResponseDTO;
import com.lugo.teams.reservs.application.service.ReservUserService;
import com.lugo.teams.reservs.application.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/dashboard/user")
@RequiredArgsConstructor
public class UserDashboardController {

    private final ReservUserService userService;
    private final ReservationService reservationService;

    @GetMapping
    public String overview(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/teams-reservs/login";
        }

        String username = principal.getName();

        ReservUserResponseDTO userDto = userService.findByUsername(username)
                .orElseGet(() -> userService.findByEmail(username).orElse(null));

        model.addAttribute("user", userDto);

        List<ReservationResponseDTO> reservations = reservationService.findByUser(username);
        model.addAttribute("reservations", reservations);

        return "dashboard/user";
    }
}
