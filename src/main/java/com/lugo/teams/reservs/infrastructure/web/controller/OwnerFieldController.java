package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.field.FieldRequestDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueResponseDTO;
import com.lugo.teams.reservs.application.service.FieldService;
import com.lugo.teams.reservs.application.service.VenueService;
import com.lugo.teams.reservs.domain.model.FieldType;
import com.lugo.teams.reservs.domain.model.SurfaceType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Gestión de canchas por owner
 * Rutas base:
 * /dashboard/owner/venues/{venueId}/fields
 */
@Controller
@RequestMapping("/dashboard/owner/venues/{venueId}/fields")
@RequiredArgsConstructor
public class OwnerFieldController {

    private final FieldService fieldService;
    private final VenueService venueService;

    /**
     * LISTADO DE CANCHAS
     */
    @GetMapping
    public String list(@PathVariable Long venueId, Model model) {

        VenueResponseDTO venue = venueService.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue no encontrado: " + venueId));

        model.addAttribute("venue", venue);
        model.addAttribute("fields", fieldService.findByVenueId(venueId));
        model.addAttribute("venueId", venueId);

        return "dashboard/owner/fields";
    }

    /**
     * FORMULARIO NUEVA CANCHA
     */
    @GetMapping("/new")
    public String newForm(@PathVariable Long venueId, Model model) {

        VenueResponseDTO venue = venueService.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue no encontrado: " + venueId));

        model.addAttribute("venue", venue);
        model.addAttribute("field", new FieldRequestDTO());
        model.addAttribute("venueId", venueId);
        model.addAttribute("fieldTypes", FieldType.values());
        model.addAttribute("surfaceTypes", SurfaceType.values());

        return "dashboard/owner/field-form";
    }

    /**
     * CREAR CANCHA
     */
    @PostMapping
    public String create(@PathVariable Long venueId,
                         @Valid @ModelAttribute("field") FieldRequestDTO dto,
                         BindingResult br,
                         Model model,
                         RedirectAttributes ra) {

        VenueResponseDTO venue = venueService.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue no encontrado: " + venueId));

        if (br.hasErrors()) {
            model.addAttribute("venue", venue);
            model.addAttribute("venueId", venueId);
            model.addAttribute("fieldTypes", FieldType.values());
            model.addAttribute("surfaceTypes", SurfaceType.values());
            return "dashboard/owner/field-form";
        }

        fieldService.createField(venueId, dto);

        ra.addFlashAttribute("success", "Cancha creada correctamente");
        return "redirect:/dashboard/owner/venues/" + venueId + "/fields";
    }
}
