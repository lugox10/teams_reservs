    package com.lugo.teams.reservs.application.dto.field;
    import jakarta.validation.constraints.*;
    import lombok.*;

    import java.math.BigDecimal;
    import java.util.List;
    import jakarta.validation.constraints.AssertTrue;

    /**
     * DTO para requests (create / update) con validaciones.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class FieldRequestDTO {

        @NotNull(message = "venueId es obligatorio")
        private Long venueId;

        @NotBlank(message = "name es obligatorio")
        private String name;

        @NotNull(message = "fieldType es obligatorio")
        private com.lugo.teams.reservs.domain.model.FieldType fieldType;

        @NotNull(message = "surface es obligatorio")
        private com.lugo.teams.reservs.domain.model.SurfaceType surface;

        @NotNull(message = "capacityPlayers es obligatorio")
        @Min(value = 1, message = "capacityPlayers mínimo 1")
        private Integer capacityPlayers;

        @NotNull(message = "pricePerHour es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "pricePerHour debe ser mayor que 0")
        private BigDecimal pricePerHour;

        private Boolean active;

        private List<String> photos;

        @NotNull(message = "slotMinutes es obligatorio")
        @Min(value = 1, message = "slotMinutes mínimo 1 (minutos)")
        private Integer slotMinutes;

        @NotNull(message = "openHour es obligatorio")
        @Min(value = 0, message = "openHour mínimo 0")
        @Max(value = 23, message = "openHour máximo 23")
        private Integer openHour;

        @NotNull(message = "closeHour es obligatorio")
        @Min(value = 1, message = "closeHour mínimo 1")
        @Max(value = 24, message = "closeHour máximo 24")
        private Integer closeHour;

        @NotNull(message = "minBookingHours es obligatorio")
        @Min(value = 1, message = "minBookingHours mínimo 1")
        private Integer minBookingHours;
        private BigDecimal lat;
        private BigDecimal lng;

        /**
         * Comprueba que la apertura sea menor que el cierre.
         * Si tu convención es que closeHour es exclusivo, la regla sigue válida:
         * openHour < closeHour
         */
        @AssertTrue(message = "openHour debe ser menor que closeHour")
        private boolean isHoursRangeValid() {
            if (openHour == null || closeHour == null) return true; // otros validadores marcan nulls
            return openHour < closeHour;
        }
    }
