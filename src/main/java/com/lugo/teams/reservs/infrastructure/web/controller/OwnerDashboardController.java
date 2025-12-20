package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueListDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueRequestDTO;
import com.lugo.teams.reservs.application.service.OwnerDashboardService;
import com.lugo.teams.reservs.application.service.VenueService;
import com.lugo.teams.reservs.application.service.FieldService;
import jakarta.servlet.http.HttpServletRequest;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controlador del dashboard del Owner. Todas las vistas devuelven nombres
 * coherentes con las plantillas que tienes en templates/dashboard/owner/.
 */
@Controller
@RequestMapping("/dashboard/owner")
@RequiredArgsConstructor
@Slf4j
public class OwnerDashboardController {

    private final OwnerDashboardService ownerDashboardService;
    private final VenueService venueService;
    private final FieldService fieldService;

    @GetMapping({"", "/"})
    public String overview(
            @RequestParam(value = "ownerId", required = false) Long ownerId,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth,
            Model model) {

        try {
            // Rango por defecto: -30 días .. +30 días (cambia aquí)
            from = (from != null) ? from : LocalDate.now().minusDays(30);
            to = (to != null) ? to : LocalDate.now().plusDays(30);

            // Resolver ownerId desde auth si no viene
            if (ownerId == null) {
                try {
                    ownerId = ownerDashboardService.getOwnerIdFromAuth(auth);
                    log.debug("OwnerId resuelto desde auth: {}", ownerId);
                } catch (Exception e) {
                    log.warn("No se pudo resolver ownerId desde auth: {}", e.getMessage());
                }
            }

            if (ownerId == null) {
                model.addAttribute("error", "No se pudo resolver tu perfil de Owner. Usa ?ownerId=<tuId> para pruebas.");
                model.addAttribute("ownerId", null);
                model.addAttribute("from", from);
                model.addAttribute("to", to);
                model.addAttribute("reservations", List.of());
                model.addAttribute("revenueByVenue", Map.of());
                model.addAttribute("metrics", defaultMetrics());
                model.addAttribute("metricsByVenue", Map.of());
                model.addAttribute("venues", List.of());
                model.addAttribute("venuesMap", Map.of());
                model.addAttribute("occupancyRatePercent", 0);
                model.addAttribute("lastReservationStartByVenue", Map.of());
                return "dashboard/owner/overview";
            }

            // Datos principales
            List<ReservationResponseDTO> reservations = ownerDashboardService.findReservationsByOwner(ownerId, from, to);

            // DEBUG: imprime tamaño e ids para verificar
            if (log.isDebugEnabled()) {
                List<Long> ids = reservations == null ? List.of() :
                        reservations.stream().map(ReservationResponseDTO::getId).collect(Collectors.toList());
                List<Long> vids = reservations == null ? List.of() :
                        reservations.stream().map(ReservationResponseDTO::getVenueId).collect(Collectors.toList());
                log.debug("overview -> reservations.size={} ids={} venueIds={}", ids.size(), ids, vids);
            }

            Map<Long, Double> revenue = ownerDashboardService.getMonthlyRevenueByVenue(ownerId, to.getYear(), to.getMonthValue());
            Map<String, Object> metrics = ownerDashboardService.getOwnerOverviewMetrics(ownerId, from, to);

            if (revenue == null) revenue = Map.of();
            if (metrics == null) metrics = defaultMetrics();

            // normaliza occupancyRate en metrics (asegura Double)
            double occupancyRateDouble = 0.0;
            Object occObj = metrics.get("occupancyRate");
            if (occObj instanceof Number n) occupancyRateDouble = n.doubleValue();
            else if (occObj != null) {
                try { occupancyRateDouble = Double.parseDouble(occObj.toString()); } catch (Exception ignored) { }
            }
            metrics.put("occupancyRate", occupancyRateDouble);
            int occupancyRatePercent = (int) Math.round(occupancyRateDouble * 100);

            // métricas por venue
            Map<Long, Map<String, Object>> metricsByVenue = ownerDashboardService.getMetricsByVenue(ownerId, from, to);
            if (metricsByVenue == null) metricsByVenue = Map.of();

            // venues y mapa id->DTO
            List<VenueListDTO> venues = venueService.findByOwnerId(ownerId);
            Map<Long, VenueListDTO> venuesMap = venues.stream()
                    .collect(Collectors.toMap(VenueListDTO::getId, v -> v, (a,b)->a, LinkedHashMap::new));

            // calcular la última reserva por venue (formateada)
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            Map<Long, String> lastReservationStartByVenue = reservations == null
                    ? Map.of()
                    : reservations.stream()
                    .filter(r -> r.getVenueId() != null)
                    .collect(Collectors.groupingBy(
                            ReservationResponseDTO::getVenueId,
                            Collectors.collectingAndThen(
                                    Collectors.maxBy(Comparator.comparing(ReservationResponseDTO::getStartDateTime)),
                                    opt -> opt.map(rr -> rr.getStartDateTime() != null ? rr.getStartDateTime().format(fmt) : "-").orElse("-")
                            )
                    ));

            // pasar todo al modelo
            model.addAttribute("reservations", reservations != null ? reservations : List.of());
            model.addAttribute("revenueByVenue", revenue);
            model.addAttribute("metrics", metrics);
            model.addAttribute("metricsByVenue", metricsByVenue);
            model.addAttribute("venues", venues);
            model.addAttribute("venuesMap", venuesMap);
            model.addAttribute("ownerId", ownerId);
            model.addAttribute("from", from);
            model.addAttribute("to", to);
            model.addAttribute("occupancyRatePercent", occupancyRatePercent);
            model.addAttribute("lastReservationStartByVenue", lastReservationStartByVenue);

            return "dashboard/owner/overview";

        } catch (Exception ex) {
            log.error("Error en overview ownerId={} : {}", ownerId, ex.getMessage(), ex);
            model.addAttribute("ownerId", ownerId);
            model.addAttribute("from", from != null ? from : null);
            model.addAttribute("to", to != null ? to : null);
            model.addAttribute("error", "Ocurrió un error cargando el dashboard: " + ex.getMessage());
            model.addAttribute("metrics", defaultMetrics());
            model.addAttribute("metricsByVenue", Map.of());
            model.addAttribute("venues", List.of());
            model.addAttribute("venuesMap", Map.of());
            model.addAttribute("revenueByVenue", Map.of());
            model.addAttribute("reservations", List.of());
            model.addAttribute("occupancyRatePercent", 0);
            model.addAttribute("lastReservationStartByVenue", Map.of());
            return "dashboard/owner/overview";
        }
    }


    @GetMapping("/venues")
    public String listVenues(@RequestParam(value = "ownerId", required = false) Long ownerId,
                             Authentication auth,
                             Model model) {
        try {
            if (ownerId == null) {
                ownerId = ownerDashboardService.getOwnerIdFromAuth(auth);
            }
            List<VenueListDTO> venues = venueService.findByOwnerId(ownerId);
            model.addAttribute("venues", venues);
            model.addAttribute("ownerId", ownerId);
            return "dashboard/owner/venues";
        } catch (Exception ex) {
            log.error("Error listVenues ownerId={} : {}", ownerId, ex.getMessage(), ex);
            model.addAttribute("ownerId", ownerId);
            model.addAttribute("error", "No se pudo cargar la lista de complejos: " + ex.getMessage());
            model.addAttribute("venues", List.of());
            return "dashboard/owner/venues";
        }
    }

    @GetMapping("/venues/new")
    public String newVenueForm(@RequestParam(value = "ownerId", required = false) Long ownerId,
                               Authentication auth,
                               Model model) {
        try {
            if (ownerId == null) {
                ownerId = ownerDashboardService.getOwnerIdFromAuth(auth);
            }
            VenueRequestDTO dto = VenueRequestDTO.builder()
                    .ownerId(ownerId)
                    .timeZone("America/Bogota")
                    .allowOnsitePayment(Boolean.TRUE)
                    .allowBankTransfer(Boolean.TRUE)
                    .allowOnlinePayment(Boolean.FALSE)
                    .build();

            model.addAttribute("venue", dto);
            model.addAttribute("ownerId", ownerId);
            return "dashboard/owner/venue-form";
        } catch (Exception e) {
            log.warn("newVenueForm: no se pudo resolver ownerId desde auth: {}", e.getMessage());
            model.addAttribute("venue", new VenueRequestDTO());
            model.addAttribute("ownerId", null);
            model.addAttribute("warning", "No se detectó tu perfil de Owner automáticamente. Puedes usar ?ownerId=<tuId> para pruebas.");
            return "dashboard/owner/venue-form";
        }
    }

    @PostMapping("/venues")
    public String createVenue(@ModelAttribute("venue") @Valid VenueRequestDTO dto,
                              BindingResult bindingResult,
                              Authentication auth,
                              RedirectAttributes ra,
                              Model model) {

        log.info("POST /dashboard/owner/venues - dto recibido: {}", dto);

        if (bindingResult.hasErrors()) {
            model.addAttribute("venue", dto);
            model.addAttribute("error", "Corrige los campos obligatorios.");
            return "dashboard/owner/venue-form";
        }

        if (dto.getOwnerId() == null) {
            try {
                Long resolved = ownerDashboardService.getOwnerIdFromAuth(auth);
                dto.setOwnerId(resolved);
            } catch (Exception e) {
                log.warn("No se pudo resolver ownerId en POST: {}", e.getMessage());
                model.addAttribute("venue", dto);
                model.addAttribute("error", "No se pudo asociar el complejo a tu perfil de Owner.");
                return "dashboard/owner/venue-form";
            }
        }

        try {
            var saved = venueService.createVenue(dto);
            log.info("Venue creada con id={} ownerId={}", saved.getId(), dto.getOwnerId());
            ra.addFlashAttribute("success", "Complejo creado correctamente (id=" + saved.getId() + ")");
            // redirect a la lista del owner (ownerId como query opcional)
            return "redirect:/dashboard/owner/venues?ownerId=" + dto.getOwnerId();
        } catch (Exception ex) {
            log.error("Error creando venue: ", ex);
            model.addAttribute("venue", dto);
            model.addAttribute("error", "Ocurrió un error creando el complejo: " + ex.getMessage());
            return "dashboard/owner/venue-form";
        }
    }

    @GetMapping("/reservations")
    public String reservations(@RequestParam(value = "ownerId", required = false) Long ownerId,
                               @RequestParam(value = "from", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                               @RequestParam(value = "to", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                               Authentication auth,
                               Model model,
                               HttpServletRequest request) { // <-- agregado
        try {
            if (ownerId == null) {
                ownerId = ownerDashboardService.getOwnerIdFromAuth(auth);
            }
            from = (from != null) ? from : LocalDate.now().minusDays(30);
            to = (to != null) ? to : LocalDate.now();

            List<ReservationResponseDTO> reservations = ownerDashboardService.findReservationsByOwner(ownerId, from, to);

            model.addAttribute("reservations", reservations);
            model.addAttribute("ownerId", ownerId);
            model.addAttribute("from", from);
            model.addAttribute("to", to);
            return "reservations/list";
        } catch (Exception ex) {
            log.error("Error loading reservations ownerId={} : {}", ownerId, ex.getMessage(), ex);
            model.addAttribute("error", "No se pudieron cargar las reservas: " + ex.getMessage());
            model.addAttribute("ownerId", ownerId);
            model.addAttribute("reservations", List.of());
            return "reservations/list";
        }
    }

    @GetMapping("/revenue")
    public String revenue(@RequestParam(value = "ownerId", required = false) Long ownerId,
                          @RequestParam(value = "year", required = false) Integer year,
                          @RequestParam(value = "month", required = false) Integer month,
                          Authentication auth,
                          Model model) {
        try {
            if (ownerId == null) {
                ownerId = ownerDashboardService.getOwnerIdFromAuth(auth);
            }
            LocalDate now = LocalDate.now();
            int y = (year != null) ? year : now.getYear();
            int m = (month != null) ? month : now.getMonthValue();

            Map<Long, Double> revenue = ownerDashboardService.getMonthlyRevenueByVenue(ownerId, y, m);
            if (revenue == null) revenue = Map.of();

            model.addAttribute("revenueByVenue", revenue); // coincide con plantilla revenue.html
            model.addAttribute("ownerId", ownerId);
            model.addAttribute("year", y);
            model.addAttribute("month", m);
            return "dashboard/owner/revenue";
        } catch (Exception ex) {
            log.error("Error loading revenue ownerId={} : {}", ownerId, ex.getMessage(), ex);
            model.addAttribute("error", "No se pudo cargar el revenue: " + ex.getMessage());
            model.addAttribute("ownerId", ownerId);
            model.addAttribute("revenueByVenue", Map.of());
            return "dashboard/owner/revenue";
        }
    }

    private Map<String, Object> defaultMetrics() {
        Map<String, Object> m = new HashMap<>();
        m.put("totalReservations", 0);
        m.put("totalRevenue", 0.0);
        m.put("occupancyRate", 0.0);
        return m;
    }
}
