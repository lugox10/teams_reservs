package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.field.FieldDetailDTO;
import com.lugo.teams.reservs.application.service.FieldService;
import com.lugo.teams.reservs.application.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/fields")
@RequiredArgsConstructor
public class PublicFieldController {

    private final FieldService fieldService;
    private final ReservationService reservationService; // puede no usarse aquí, pero queda por si quieres lógica adicional

    /**
     * Página pública de detalle de cancha (punto único de reserva).
     * GET /fields/{fieldId}
     */
    @GetMapping("/{fieldId}")
    public String detail(@PathVariable Long fieldId, Model model) {
        Optional<FieldDetailDTO> opt = fieldService.findDetailById(fieldId);
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cancha no encontrada: " + fieldId);
        }
        FieldDetailDTO field = opt.get();

        model.addAttribute("field", field);
        model.addAttribute("fieldId", fieldId);
        model.addAttribute("venueId", field.getVenueId());
        model.addAttribute("venueName", field.getVenueName());

        return "fields/detail";
    }

    /**
     * Endpoint usado por JS para obtener horas ocupadas en una fecha.
     * GET /fields/{fieldId}/availability?date=2025-12-10
     *
     * Devuelve lista de enteros (horas, 0..23) ocupadas.
     */
    @GetMapping("/{fieldId}/availability")
    @ResponseBody
    public List<Integer> availability(
            @PathVariable Long fieldId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // FieldService debe exponer un método que devuelva List<LocalTime> o similar
        List<LocalTime> booked = fieldService.getBookedHoursForDate(fieldId, date);
        return booked.stream()
                .map(LocalTime::getHour)
                .sorted()
                .collect(Collectors.toList());
    }
}
