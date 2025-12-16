package com.lugo.teams.reservs.shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model) {
        log.error("Error capturado globalmente: ", ex);
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("stackTrace", ex.getStackTrace());
        return "error/custom-error"; // crea plantilla Thymeleaf custom-error.html
    }
}

