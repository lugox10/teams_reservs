package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.venue.VenueListDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueResponseDTO;
import com.lugo.teams.reservs.application.dto.field.FieldSummaryDTO;
import com.lugo.teams.reservs.application.service.VenueService;
import com.lugo.teams.reservs.application.service.FieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueWebController {

    private final VenueService venueService;
    private final FieldService fieldService;

    // Lista pública de venues (grid con foto/logo)
    @GetMapping
    public String listVenues(Model model) {
        List<VenueResponseDTO> venues = venueService.findActive(); // ajusta si usas otro método
        model.addAttribute("venues", venues);
        return "venues/list";
    }

    // Detalle del venue y listado de fields
    @GetMapping("/{id}")
    public String venueDetail(@PathVariable("id") Long id, Model model) {
        VenueResponseDTO venue = venueService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venue no encontrado: " + id));
        List<FieldSummaryDTO> fields = fieldService.findSummariesByVenueId(id); // agrega este método si no existe
        model.addAttribute("venue", venue);
        model.addAttribute("fields", fields);
        return "venues/detail";
    }
}
