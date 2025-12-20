package com.lugo.teams.reservs.shared.exception;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    private final ErrorAttributes errorAttributes;

    public GlobalExceptionHandler(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    /**
     * Atrapa cualquier excepción no manejada, prepara respuesta JSON para AJAX/API
     * o ModelAndView para vistas HTML. Evita casteos peligrosos y provee fallback.
     */
    @ExceptionHandler(Exception.class)
    public Object handleAllExceptions(HttpServletRequest request, Exception ex) {
        try {
            // Usamos ServletWebRequest (construido desde HttpServletRequest) — seguro con Spring Security
            ServletWebRequest swr = new ServletWebRequest(request);

            ErrorAttributeOptions options = ErrorAttributeOptions.of(
                    ErrorAttributeOptions.Include.MESSAGE,
                    ErrorAttributeOptions.Include.STACK_TRACE,
                    ErrorAttributeOptions.Include.BINDING_ERRORS
            );

            Map<String, Object> details = errorAttributes.getErrorAttributes(swr, options);

            Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            int status = (statusAttr != null) ? parseStatus(statusAttr) : HttpStatus.INTERNAL_SERVER_ERROR.value();

            // Log con contexto mínimo
            log.warn("GlobalExceptionHandler -> status={} path={} message={}", status, request.getRequestURI(), ex.getMessage(), ex);

            if (isApiRequest(request)) {
                // Respuesta JSON para AJAX / APIs
                Map<String, Object> body = new HashMap<>(details);
                body.put("timestamp", Instant.now().toString());
                body.put("path", request.getRequestURI());
                body.put("status", status);
                // No exponemos toda la traza en producción; esto es útil en desarrollo.
                return ResponseEntity.status(status)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body);
            } else {
                // Respuesta para vistas HTML (Thymeleaf)
                ModelAndView mav = new ModelAndView("error");
                mav.addObject("status", status);
                mav.addObject("errorDetails", details);
                mav.addObject("message", ex.getMessage());
                mav.addObject("path", request.getRequestURI());
                return mav;
            }
        } catch (Exception handlerEx) {
            // Fallback seguro — nunca lanzar más excepciones desde el manejador de errores
            log.error("Error en GlobalExceptionHandler al procesar la excepción original", handlerEx);
            if (isProbablyApiCall(request)) {
                Map<String, Object> fallback = Map.of(
                        "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "message", ex.getMessage()
                );
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(fallback);
            } else {
                ModelAndView mav = new ModelAndView("error");
                mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
                mav.addObject("errorDetails", Map.of("message", ex.getMessage()));
                mav.addObject("message", ex.getMessage());
                return mav;
            }
        }
    }

    // --------- Helpers ---------

    private int parseStatus(Object statusAttr) {
        try {
            return Integer.parseInt(statusAttr.toString());
        } catch (Exception e) {
            return HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
    }

    /**
     * Decide si la petición es AJAX / API (espera JSON) — revisa Accept, X-Requested-With o path /api.
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String xhr = request.getHeader("X-Requested-With");
        String uri = request.getRequestURI() == null ? "" : request.getRequestURI();

        return (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) ||
                "XMLHttpRequest".equalsIgnoreCase(xhr) ||
                uri.startsWith("/api/");
    }

    /**
     * Versión conservadora para usar en fallback (no lanza NPE si request es null).
     */
    private boolean isProbablyApiCall(HttpServletRequest request) {
        if (request == null) return false;
        return isApiRequest(request);
    }

    @ExceptionHandler(ClassCastException.class)
    public String handleClassCast(ClassCastException ex, Model model) {
        model.addAttribute("error", "Error temporal de desarrollo: " + ex.getMessage());
        model.addAttribute("reservations", List.of());
        return "dashboard/owner/reservations"; // o la vista genérica que uses
    }
}
