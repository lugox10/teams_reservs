package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.field.FieldDetailDTO;
import com.lugo.teams.reservs.application.dto.field.FieldRequestDTO;

import com.lugo.teams.reservs.application.service.FieldService;
import com.lugo.teams.reservs.domain.model.FieldType;
import com.lugo.teams.reservs.domain.model.SurfaceType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/dashboard/owner/venues/{venueId}/fields")
@RequiredArgsConstructor
public class OwnerFieldController {

    private final FieldService fieldService;

    @GetMapping
    public String list(@PathVariable Long venueId, Model model) {
        model.addAttribute("fields", fieldService.findByVenueId(venueId));
        model.addAttribute("venueId", venueId);
        return "dashboard/owner/fields";
    }

    @GetMapping("/new")
    public String newForm(@PathVariable Long venueId, Model model) {
        FieldRequestDTO dto = new FieldRequestDTO();
        dto.setVenueId(venueId);

        model.addAttribute("field", dto);
        model.addAttribute("venueId", venueId);
        model.addAttribute("fieldTypes", FieldType.values());
        model.addAttribute("surfaceTypes", SurfaceType.values());

        return "dashboard/owner/field-form";
    }

    @PostMapping
    public String create(@PathVariable Long venueId,
                         @Valid @ModelAttribute("field") FieldRequestDTO dto,
                         BindingResult br,
                         RedirectAttributes ra,
                         Model model) {

        if (br.hasErrors()) {
            model.addAttribute("fieldTypes", FieldType.values());
            model.addAttribute("surfaceTypes", SurfaceType.values());
            return "dashboard/owner/field-form";
        }

        dto.setVenueId(venueId);
        fieldService.createField(dto);

        ra.addFlashAttribute("success", "Cancha creada correctamente");
        return "redirect:/dashboard/owner/venues/" + venueId + "/fields";
    }
}
