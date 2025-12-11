package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.user.ReservUserRequestDTO;
import com.lugo.teams.reservs.application.service.ReservUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reserv-users")
public class ReservUserController {

    private static final Logger log = LoggerFactory.getLogger(ReservUserController.class);

    private final ReservUserService userService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new ReservUserRequestDTO());
        return "reserv-users/register"; // crear plantilla Thymeleaf
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") ReservUserRequestDTO dto,
                           BindingResult br,
                           RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "reserv-users/register";
        }
        try {
            var created = userService.register(dto);
            ra.addFlashAttribute("success", "Usuario registrado. Inicia sesión.");
            return "redirect:/teams-reservs/login"; // o la ruta que uses
        } catch (Exception ex) {
            log.warn("Error registrando usuario: {}", ex.getMessage());
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/reserv-users/register";
        }
    }
}
