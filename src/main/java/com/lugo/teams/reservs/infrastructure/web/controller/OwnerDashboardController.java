package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueListDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueRequestDTO;
import com.lugo.teams.reservs.application.service.FieldService;
import com.lugo.teams.reservs.application.service.OwnerDashboardService;
import com.lugo.teams.reservs.application.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard/owner")
@RequiredArgsConstructor
@Slf4j
public class OwnerDashboardController {

    private final OwnerDashboardService ownerDashboardService;
    private final VenueService venueService;
    private final FieldService fieldService;

    // ------------------ Overview ------------------
    @GetMapping
    public String overview(@RequestParam("ownerId") Long ownerId,
                           @RequestParam(value = "from", required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                           @RequestParam(value = "to", required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                           Model model) {

        from = (from != null) ? from : LocalDate.now().minusDays(30);
        to = (to != null) ? to : LocalDate.now();

        List<ReservationResponseDTO> reservations = ownerDashboardService.findReservationsByOwner(ownerId, from, to);
        Map<Long, Double> revenue = ownerDashboardService.getMonthlyRevenueByVenue(ownerId, to.getYear(), to.getMonthValue());
        Map<String, Object> metrics = ownerDashboardService.getOwnerOverviewMetrics(ownerId, from, to);

        model.addAttribute("reservations", reservations);
        model.addAttribute("revenue", revenue);
        model.addAttribute("metrics", metrics);
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "dashboard/owner/overview";
    }

    // ------------------ Reservations ------------------
    @GetMapping("/reservations")
    public String reservations(@RequestParam("ownerId") Long ownerId,
                               @RequestParam(value = "from", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam(value = "to", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                               Model model) {

        from = (from != null) ? from : LocalDate.now().minusDays(30);
        to = (to != null) ? to : LocalDate.now();

        List<ReservationResponseDTO> reservations = ownerDashboardService.findReservationsByOwner(ownerId, from, to);

        model.addAttribute("reservations", reservations);
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "dashboard/owner/reservations";
    }

    // ------------------ Revenue ------------------
    @GetMapping("/revenue")
    public String revenue(@RequestParam("ownerId") Long ownerId,
                          @RequestParam(value = "year", required = false) Integer year,
                          @RequestParam(value = "month", required = false) Integer month,
                          Model model) {

        LocalDate now = LocalDate.now();
        int effectiveYear = (year != null) ? year : now.getYear();
        int effectiveMonth = (month != null) ? month : now.getMonthValue();

        Map<Long, Double> revenueByVenue = ownerDashboardService.getMonthlyRevenueByVenue(ownerId, effectiveYear, effectiveMonth);

        model.addAttribute("revenueByVenue", revenueByVenue);
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("year", effectiveYear);
        model.addAttribute("month", effectiveMonth);

        return "dashboard/owner/revenue";
    }

    // ------------------ Venues ------------------
    @GetMapping("/venues")
    public String listVenues(@RequestParam("ownerId") Long ownerId, Model model) {
        List<VenueListDTO> venues = venueService.findByOwnerId(ownerId);
        model.addAttribute("venues", venues);
        model.addAttribute("ownerId", ownerId);
        return "dashboard/owner/venues";
    }

    @GetMapping("/venues/new")
    public String newVenueForm(Model model, Authentication auth) {
        try {
            Long ownerId = ownerDashboardService.getOwnerIdFromAuth(auth);
            model.addAttribute("venue", new VenueRequestDTO());
            model.addAttribute("ownerId", ownerId);
        } catch (Exception e) {
            log.warn("No se pudo obtener ownerId desde auth: {}", e.getMessage());
            model.addAttribute("venue", new VenueRequestDTO());
            model.addAttribute("ownerId", null);
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "dashboard/owner/venue-form";
    }

    @PostMapping("/venues")
    public String createVenue(@ModelAttribute("venue") @Valid VenueRequestDTO dto,
                              BindingResult bindingResult,
                              Authentication auth,
                              RedirectAttributes ra,
                              Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("venue", dto);
            return "dashboard/owner/venue-form";
        }

        if (dto.getOwnerId() == null) {
            Long ownerId = ownerDashboardService.getOwnerIdFromAuth(auth);
            dto.setOwnerId(ownerId);
        }

        venueService.createVenue(dto);
        ra.addFlashAttribute("success", "Complejo creado correctamente");

        return "redirect:/dashboard/owner/venues?ownerId=" + dto.getOwnerId();
    }
}
