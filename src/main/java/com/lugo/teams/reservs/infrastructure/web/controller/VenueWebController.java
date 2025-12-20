package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.venue.VenueResponseDTO;
import com.lugo.teams.reservs.application.dto.field.FieldSummaryDTO;
import com.lugo.teams.reservs.application.service.VenueService;
import com.lugo.teams.reservs.application.service.FieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador público para listar y ver detalle de venues.
 * Rutas públicas: /venues  y /venues/{id}
 */
@Controller
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueWebController {

    private final VenueService venueService;
    private final FieldService fieldService;

    @GetMapping
    public String listVenues(Model model) {
        List<VenueResponseDTO> venues = venueService.findActive();
        model.addAttribute("venues", venues);
        return "venues/list";
    }

    @GetMapping("/{id}")
    public String venueDetail(@PathVariable("id") Long id, Model model) {
        VenueResponseDTO venue = venueService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venue no encontrado: " + id));
        List<FieldSummaryDTO> fields = fieldService.findSummariesByVenueId(id);
        model.addAttribute("venue", venue);
        model.addAttribute("fields", fields);
        return "venues/detail";
    }
}
