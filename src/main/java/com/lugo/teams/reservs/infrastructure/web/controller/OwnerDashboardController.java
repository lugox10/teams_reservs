package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import com.lugo.teams.reservs.application.service.OwnerDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dashboard/owner")
@RequiredArgsConstructor

public class OwnerDashboardController {

    private static final Logger log = LoggerFactory.getLogger(OwnerDashboardController.class);
    private final OwnerDashboardService ownerDashboardService;

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
}
