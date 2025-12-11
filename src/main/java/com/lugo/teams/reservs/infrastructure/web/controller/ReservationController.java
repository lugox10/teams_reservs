package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.field.FieldDTO;
import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import com.lugo.teams.reservs.application.dto.reserv.ReservationRequestDTO;
import com.lugo.teams.reservs.application.dto.slot.TimeSlotDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueResponseDTO;
import com.lugo.teams.reservs.application.service.FieldService;
import com.lugo.teams.reservs.application.service.ReservationService;
import com.lugo.teams.reservs.application.service.TimeSlotService;
import com.lugo.teams.reservs.application.service.VenueService;
import com.lugo.teams.reservs.application.service.PaymentService;
import com.lugo.teams.reservs.application.service.ReservUserService;
import com.lugo.teams.reservs.shared.exception.BadRequestException;
import com.lugo.teams.reservs.shared.exception.ConflictException;
import com.lugo.teams.reservs.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    private final ReservationService reservationService;
    private final FieldService fieldService;
    private final TimeSlotService timeSlotService;
    private final VenueService venueService;
    private final PaymentService paymentService;
    private final ReservUserService userService; // <-- nuevo

    // LIST
    @GetMapping
    public String list(@RequestParam(required = false) String userName, Model model, Principal principal) {
        String user = userName;
        if (user == null || user.isBlank()) {
            user = principal != null ? principal.getName() : null;
        }
        if (user == null) {
            model.addAttribute("reservations", List.of());
        } else {
            var list = reservationService.findByUser(user);
            model.addAttribute("reservations", list);
        }
        return "reservations/list";
    }

    // NEW form
    @GetMapping("/new")
    public String newForm(Model model, Principal principal) {
        model.addAttribute("reservation", new ReservationRequestDTO());
        List<VenueResponseDTO> venues = venueService.findActive();
        model.addAttribute("venues", venues);
        model.addAttribute("fields", List.of());
        model.addAttribute("timeSlots", List.of());

        // si hay principal, pre-fill userName y userId si es posible
        if (principal != null) {
            String name = principal.getName();
            model.addAttribute("prefillUserName", name);
            userService.findByUsername(name).ifPresent(u -> model.addAttribute("prefillUserId", u.getId()));
            userService.findByEmail(name).ifPresent(u -> model.addAttribute("prefillUserId", u.getId()));
        }

        return "reservations/form";
    }

    // CREATE
    @PostMapping
    public String create(@Valid @ModelAttribute("reservation") ReservationRequestDTO dto,
                         BindingResult br,
                         RedirectAttributes ra,
                         Principal principal,
                         Model model) {
        if (br.hasErrors()) {
            model.addAttribute("venues", venueService.findActive());
            if (dto.getVenueId() != null) model.addAttribute("fields", fieldService.findByVenueId(dto.getVenueId()));
            if (dto.getFieldId() != null) model.addAttribute("timeSlots", timeSlotService.findByFieldId(dto.getFieldId()));
            return "reservations/form";
        }

        // set userName and userId from principal if available
        if (principal != null) {
            String principalName = principal.getName();
            if (dto.getUserName() == null || dto.getUserName().isBlank()) {
                dto.setUserName(principalName);
            }
            userService.findByUsername(principalName).ifPresent(u -> dto.setUserId(u.getId()));
            if (dto.getUserId() == null) {
                userService.findByEmail(principalName).ifPresent(u -> dto.setUserId(u.getId()));
            }
        }

        try {
            var created = reservationService.createReservation(dto);
            ra.addFlashAttribute("success", "Reserva creada correctamente (id=" + created.getId() + ")");
            return "redirect:/reservations";
        } catch (BadRequestException | NotFoundException | ConflictException ex) {
            log.warn("Error creando reserva: {}", ex.getMessage());
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/reservations/new";
        } catch (Exception ex) {
            log.error("Error inesperado creando reserva", ex);
            ra.addFlashAttribute("error", "Error interno al crear la reserva");
            return "redirect:/reservations/new";
        }
    }

    // DETAIL
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var opt = reservationService.findById(id);
        if (opt.isEmpty()) {
            model.addAttribute("error", "Reserva no encontrada: " + id);
            return "redirect:/reservations";
        }
        model.addAttribute("reservation", opt.get());
        return "reservations/detail";
    }

    // EDIT form
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        var opt = reservationService.findById(id);
        if (opt.isEmpty()) {
            model.addAttribute("error", "Reserva no encontrada: " + id);
            return "redirect:/reservations";
        }
        ReservationResponseDTO resp = opt.get();
        ReservationRequestDTO req = new ReservationRequestDTO();
        req.setFieldId(resp.getFieldId());
        req.setStartDateTime(resp.getStartDateTime());
        req.setEndDateTime(resp.getEndDateTime());
        req.setPlayersCount(resp.getPlayersCount());
        req.setTeamName(resp.getTeamName());
        req.setNotes(resp.getNotes());
        req.setUserName(resp.getUserName());
        req.setTimeSlotId(resp.getTimeSlotId());

        model.addAttribute("reservation", req);
        model.addAttribute("venues", venueService.findActive());
        if (resp.getVenueId() != null) model.addAttribute("fields", fieldService.findByVenueId(resp.getVenueId()));
        if (resp.getFieldId() != null) model.addAttribute("timeSlots", timeSlotService.findByFieldId(resp.getFieldId()));
        model.addAttribute("editingId", id);
        return "reservations/form";
    }

    // CANCEL
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra, Principal principal) {
        try {
            boolean changed = reservationService.cancelReservation(id, principal != null ? principal.getName() : "system");
            if (changed) ra.addFlashAttribute("success", "Reserva cancelada");
            else ra.addFlashAttribute("info", "Reserva ya estaba cancelada");
        } catch (NotFoundException | BadRequestException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/reservations";
    }

    // INITIATE PAYMENT
    @PostMapping("/{id}/initiate-payment")
    public String initiatePayment(@PathVariable Long id, RedirectAttributes ra) {
        try {
            var result = paymentService.initiatePayment(id, null, false);
            ra.addFlashAttribute("success", "Iniciado checkout. Redirigiendo...");
            return "redirect:" + result.getCheckoutUrl();
        } catch (Exception ex) {
            log.error("Error iniciando pago", ex);
            ra.addFlashAttribute("error", "No se pudo iniciar el pago: " + ex.getMessage());
            return "redirect:/reservations/" + id;
        }
    }

    // JSON endpoints para selects dinámicos (AJAX)
    @GetMapping("/api/fields")
    @ResponseBody
    public List<FieldDTO> findFieldsByVenue(@RequestParam Long venueId) {
        return fieldService.findByVenueId(venueId);
    }

    @GetMapping("/api/timeslots")
    @ResponseBody
    public List<TimeSlotDTO> findTimeSlotsByField(@RequestParam Long fieldId) {
        return timeSlotService.findByFieldId(fieldId);
    }

    // UPDATE handler
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("reservation") ReservationRequestDTO dto,
                         BindingResult br,
                         RedirectAttributes ra,
                         Principal principal,
                         Model model) {

        if (principal != null) {
            String principalName = principal.getName();
            if (dto.getUserName() == null || dto.getUserName().isBlank()) {
                dto.setUserName(principalName);
            }
            userService.findByUsername(principalName).ifPresent(u -> dto.setUserId(u.getId()));
            if (dto.getUserId() == null) {
                userService.findByEmail(principalName).ifPresent(u -> dto.setUserId(u.getId()));
            }
        }

        if (br.hasErrors()) {
            model.addAttribute("venues", venueService.findActive());
            if (dto.getVenueId() != null) model.addAttribute("fields", fieldService.findByVenueId(dto.getVenueId()));
            if (dto.getFieldId() != null) model.addAttribute("timeSlots", timeSlotService.findByFieldId(dto.getFieldId()));
            model.addAttribute("editingId", id);
            return "reservations/form";
        }

        try {
            var updated = reservationService.updateReservation(id, dto);
            ra.addFlashAttribute("success", "Reserva actualizada (id=" + updated.getId() + ")");
            return "redirect:/reservations";
        } catch (BadRequestException | NotFoundException | ConflictException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/reservations/" + id + "/edit";
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "Error interno actualizando reserva");
            log.error("Error update reservation", ex);
            return "redirect:/reservations/" + id + "/edit";
        }
    }
}
