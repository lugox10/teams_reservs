package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.venue.VenueDetailDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueListDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueResponseDTO;
import com.lugo.teams.reservs.application.mapper.VenueMapper;
import com.lugo.teams.reservs.application.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/venues")
public class VenueWebController {

    private final VenueService venueService;
    private final VenueMapper venueMapper;

    /**
     * GET /venues
     * Página con lista de venues (cards). Usa venueService.findActive() que devuelve List<VenueListDTO>.
     */
    @GetMapping
    public String listAll(Model model) {
        List<VenueResponseDTO> venues = venueService.findActive();
        model.addAttribute("venues", venues);
        return "venues/list"; // crea templates/venues/list.html
    }

    /**
     * GET /venues/{id}
     * Página detalle de venue + lista de fields (usamos VenueDetailDTO).
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var opt = venueService.findById(id);
        if (opt.isEmpty()) {
            model.addAttribute("error", "Complejo no encontrado: " + id);
            return "redirect:/venues";
        }
        VenueResponseDTO dto = opt.get();
        model.addAttribute("venue", dto);
        // para comodidad del fragment/JS: pasar venueId y some basic info
        model.addAttribute("venueId", dto.getId());
        model.addAttribute("venueName", dto.getName());
        return "venues/detail"; // crea templates/venues/detail.html y usa dto.fields
    }
}
