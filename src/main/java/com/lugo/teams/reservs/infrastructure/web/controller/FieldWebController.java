package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.field.FieldDetailDTO;
import com.lugo.teams.reservs.application.mapper.FieldMapper;
import com.lugo.teams.reservs.application.service.FieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/fields")
public class FieldWebController {

    private final FieldService fieldService;
    private final FieldMapper fieldMapper;

    /**
     * GET /fields/{id}/detail
     * Página detalle de la cancha. Incluye el fragment availability que hace fetch a /api/fields/{id}/availability.
     */
    @GetMapping("/{id}/detail")
    public String fieldDetailPage(@PathVariable Long id, Model model) {
        // Preferimos que fieldService devuelva FieldDetailDTO; si no, intentamos convertir con mapper
        var optDto = fieldService.findDetailById(id); // Optional<FieldDetailDTO>
        if (optDto != null && optDto.isPresent()) {
            FieldDetailDTO fd = optDto.get();
            model.addAttribute("field", fd);
            model.addAttribute("fieldId", fd.getId());
            model.addAttribute("fieldName", fd.getName());
            model.addAttribute("fieldType", fd.getFieldType());
            model.addAttribute("surface", fd.getSurface());
            model.addAttribute("pricePerHour", fd.getPricePerHour());
            model.addAttribute("openHour", fd.getOpenHour());
            model.addAttribute("closeHour", fd.getCloseHour());
            model.addAttribute("slotMinutes", fd.getSlotMinutes());
            model.addAttribute("venueId", fd.getVenueId());
            model.addAttribute("venueName", fd.getVenueName());
            return "fields/detail";
        }

        // Fallback: maybe service returns entity or FieldDTO; try to get via alternative method
        var optBasic = fieldService.findById(id); // Optional<FieldDTO>
        if (optBasic != null && optBasic.isPresent()) {
            var basic = optBasic.get();
            // map basic DTO -> FieldDetailDTO minimally so the view can render
            FieldDetailDTO fd = FieldDetailDTO.builder()
                    .id(basic.getId())
                    .venueId(basic.getVenueId())
                    .venueName(null)
                    .name(basic.getName())
                    .fieldType(basic.getFieldType())
                    .surface(basic.getSurface())
                    .capacityPlayers(basic.getCapacityPlayers())
                    .pricePerHour(basic.getPricePerHour())
                    .slotMinutes(basic.getSlotMinutes())
                    .openHour(basic.getOpenHour())
                    .closeHour(basic.getCloseHour())
                    .minBookingHours(basic.getMinBookingHours())
                    .photos(basic.getPhotos())
                    .paymentOptions(java.util.List.of()) // not available here
                    .build();

            model.addAttribute("field", fd);
            model.addAttribute("fieldId", fd.getId());
            model.addAttribute("fieldName", fd.getName());
            model.addAttribute("fieldType", fd.getFieldType());
            model.addAttribute("surface", fd.getSurface());
            model.addAttribute("pricePerHour", fd.getPricePerHour());
            model.addAttribute("openHour", fd.getOpenHour());
            model.addAttribute("closeHour", fd.getCloseHour());
            model.addAttribute("slotMinutes", fd.getSlotMinutes());
            model.addAttribute("venueId", fd.getVenueId());
            model.addAttribute("venueName", fd.getVenueName());
            return "fields/detail";
        }

        model.addAttribute("error", "Cancha no encontrada: " + id);
        return "redirect:/venues";
    }
}
