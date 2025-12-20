package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.field.FieldDetailDTO;
import com.lugo.teams.reservs.application.service.FieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/fields")
@RequiredArgsConstructor
public class PublicFieldController {

    private final FieldService fieldService;

    // ===============================
    // DETALLE DE CANCHA (WEB / THYMELEAF)
    // ===============================
    @GetMapping("/{fieldId}")
    public String detail(@PathVariable Long fieldId, Model model) {

        FieldDetailDTO field = fieldService.findDetailById(fieldId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Cancha no encontrada"));

        model.addAttribute("field", field);
        model.addAttribute("fieldId", fieldId);

        List<String> paymentOptions = field.getPaymentOptions() != null ? field.getPaymentOptions() : List.of();
        boolean allowOnsite = paymentOptions.stream().anyMatch(s -> "ONSITE".equalsIgnoreCase(s));
        boolean allowBank = paymentOptions.stream().anyMatch(s -> "BANK".equalsIgnoreCase(s) || "TRANSFER".equalsIgnoreCase(s));
        boolean allowOnline = paymentOptions.stream().anyMatch(s -> "ONLINE".equalsIgnoreCase(s));

        model.addAttribute("allowOnsitePayment", allowOnsite);
        model.addAttribute("allowBankTransfer", allowBank);
        model.addAttribute("allowOnlinePayment", allowOnline);

        model.addAttribute("openHour", field.getOpenHour() != null ? field.getOpenHour() : 6);
        model.addAttribute("closeHour", field.getCloseHour() != null ? field.getCloseHour() : 23);
        model.addAttribute("pricePerHour", field.getPricePerHour() != null ? field.getPricePerHour() : 0);
        model.addAttribute("slotMinutes", field.getSlotMinutes() != null ? field.getSlotMinutes() : 60);
        model.addAttribute("minBookingHours", field.getMinBookingHours() != null ? field.getMinBookingHours() : 1);

        model.addAttribute("venueId", field.getVenueId());
        model.addAttribute("venueName", field.getVenueName());
        model.addAttribute("paymentOptions", paymentOptions);

        // ------ >>> AÑADIR: ReservationRequestDTO prefilled para el formulario <<<
        var reservationForm = new com.lugo.teams.reservs.application.dto.reserv.ReservationRequestDTO();
        reservationForm.setFieldId(fieldId);
        reservationForm.setVenueId(field.getVenueId());
        // duration default ya está en DTO (60), playersCount también
        model.addAttribute("reservation", reservationForm);

        return "fields/detail";
    }


    // ===============================
    // DISPONIBILIDAD POR FECHA (AJAX / JSON)
    // ===============================
    @GetMapping("/{fieldId}/availability")
    @ResponseBody
    public List<Integer> availability(
            @PathVariable Long fieldId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return fieldService.getBookedHoursForDate(fieldId, date)
                .stream()
                .map(LocalTime::getHour)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
