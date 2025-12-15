package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.field.FieldDTO;
import com.lugo.teams.reservs.application.dto.field.FieldDetailDTO;
import com.lugo.teams.reservs.application.dto.field.FieldSummaryDTO;
import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueListDTO;
import com.lugo.teams.reservs.application.service.OwnerDashboardService;
import com.lugo.teams.reservs.application.dto.venue.VenueRequestDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueResponseDTO;
import com.lugo.teams.reservs.application.dto.field.FieldRequestDTO;

import com.lugo.teams.reservs.application.service.VenueService;
import com.lugo.teams.reservs.application.service.FieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard/owner")
@RequiredArgsConstructor

public class OwnerDashboardController {

    private static final Logger log = LoggerFactory.getLogger(OwnerDashboardController.class);
    private final OwnerDashboardService ownerDashboardService;
    private final VenueService venueService;
    private final FieldService fieldService;

    @GetMapping
    public String overview(@RequestParam("ownerId") Long ownerId,
                           @RequestParam(value = "from", required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                           @RequestParam(value = "to", required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                           Model model) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();
        List<ReservationResponseDTO> reservations = ownerDashboardService.findReservationsByOwner(ownerId, from, to);
        Map<Long, Double> revenue = ownerDashboardService.getMonthlyRevenueByVenue(ownerId, to.getYear(), to.getMonthValue());
        Map<String, Object> metrics = ownerDashboardService.getOwnerOverviewMetrics(ownerId, from, to);

        model.addAttribute("reservations", reservations);
        model.addAttribute("revenue", revenue);
        model.addAttribute("metrics", metrics);
        model.addAttribute("ownerId", ownerId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "dashboard/owner/overview"; // crea plantilla correspondiente
    }
    /**
     * Lista de reservas del dueño en un rango de fechas.
     *
     * GET /dashboard/owner/reservations?ownerId=1&from=2025-01-01&to=2025-01-31
     */
    @GetMapping("/reservations")
    public String reservations(@RequestParam("ownerId") Long ownerId,
                               @RequestParam(value = "from", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam(value = "to", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                               Model model) {

        List<ReservationResponseDTO> reservations =
                ownerDashboardService.findReservationsByOwner(ownerId, from, to);

        model.addAttribute("ownerId", ownerId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("reservations", reservations);

        // Thymeleaf template: src/main/resources/templates/dashboard/owner/reservations.html
        return "dashboard/owner/reservations";
    }

    /**
     * Resumen de ingresos por sede en un mes.
     *
     * GET /dashboard/owner/revenue?ownerId=1&year=2025&month=1
     * Si no mandas year/month, se usa el mes actual.
     */
    @GetMapping("/revenue")
    public String revenue(@RequestParam("ownerId") Long ownerId,
                          @RequestParam(value = "year", required = false) Integer year,
                          @RequestParam(value = "month", required = false) Integer month,
                          Model model) {

        LocalDate now = LocalDate.now();
        int effectiveYear = (year != null) ? year : now.getYear();
        int effectiveMonth = (month != null) ? month : now.getMonthValue();

        Map<Long, Double> revenueByVenue =
                ownerDashboardService.getMonthlyRevenueByVenue(ownerId, effectiveYear, effectiveMonth);

        model.addAttribute("ownerId", ownerId);
        model.addAttribute("year", effectiveYear);
        model.addAttribute("month", effectiveMonth);
        model.addAttribute("revenueByVenue", revenueByVenue);

        // Thymeleaf template: src/main/resources/templates/dashboard/owner/revenue.html
        return "dashboard/owner/revenue";
    }

    // Listar venues del owner
    @GetMapping("/venues")
    public String listVenues(@RequestParam("ownerId") Long ownerId, Model model) {
        List<VenueListDTO> venues = venueService.findByOwnerId(ownerId);
        model.addAttribute("venues", venues);
        model.addAttribute("ownerId", ownerId);
        return "dashboard/owner/venues";
    }

    // Formulario para crear venue
    @GetMapping("/venues/new")
    public String newVenueForm(@RequestParam("ownerId") Long ownerId, Model model) {
        model.addAttribute("venue", new VenueRequestDTO());
        model.addAttribute("ownerId", ownerId);
        return "dashboard/owner/venue-form";
    }

    // Guardar venue
    @PostMapping("/venues")

    public String createVenue(@RequestParam("ownerId") Long ownerId,
                             @ModelAttribute("venue") VenueRequestDTO dto,
                             BindingResult br,
                             RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "dashboard/owner/venue-form";
        }
        dto.setOwnerId(ownerId);
        venueService.createVenue(dto);
        ra.addFlashAttribute("success", "Complejo creado correctamente");
        return "redirect:/dashboard/owner/venues?ownerId=" + ownerId;
    }


}
