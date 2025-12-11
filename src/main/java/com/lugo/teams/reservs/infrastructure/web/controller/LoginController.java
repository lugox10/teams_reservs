package com.lugo.teams.reservs.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/teams-reservs")
@RequiredArgsConstructor
public class LoginController {

    // GET /teams-reservs/login
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            Model model) {
        model.addAttribute("error", error);
        return "teams-reservs/login"; // apunta a src/main/resources/templates/teams-reservs/login.html
    }
}
