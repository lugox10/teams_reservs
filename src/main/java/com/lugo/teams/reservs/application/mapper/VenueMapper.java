package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.owner.OwnerSummaryDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueRequestDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueResponseDTO;
import com.lugo.teams.reservs.domain.model.Owner;
import com.lugo.teams.reservs.domain.model.Venue;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VenueMapper {

    // ================== Entity -> VenueResponseDTO ==================
    public VenueResponseDTO toResponseDTO(Venue v) {
        if (v == null) return null;
        VenueResponseDTO dto = VenueResponseDTO.builder()
                .id(v.getId())
                .nombre(v.getName())
                .direccion(v.getAddress())
                .tipoDeporte(null) // <- la entidad Venue NO tiene campo tipoDeporte hoy. Añadir en entidad si lo necesitas.
                .active(v.isActive())
                .photos(v.getPhotos() != null ? v.getPhotos() : Collections.emptyList())
                .timeZone(v.getTimeZone())
                .owner(null)
                .build();

        if (v.getOwner() != null) {
            OwnerSummaryDTO o = new OwnerSummaryDTO();
            o.setId(v.getOwner().getId());
            o.setNombre(v.getOwner().getName());
            dto.setOwner(o);
        }
        return dto;
    }

    public List<VenueResponseDTO> toResponseDTOList(List<Venue> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    // ================== RequestDTO -> Entity (crear) ==================
    public Venue toEntity(VenueRequestDTO req, Owner owner) {
        if (req == null) return null;
        Venue v = new Venue();
        // id normalmente lo setea JPA; si vas a usar para update pasa por updateEntityFromRequest
        v.setName(req.getNombre());
        v.setAddress(req.getDireccion());
        v.setTimeZone(req.getTimeZone());
        v.setActive(true); // por defecto active = true al crear; si querés tomar from DTO agrega campo
        v.setPhotos(req.getPhotos() != null ? req.getPhotos() : Collections.emptyList());
        v.setOwner(owner);
        // tipoDeporte: la entidad Venue no tiene este campo. Si querés persistirlo,
        // agrega atributo tipoDeporte en Venue y descomenta la línea siguiente
        // v.setTipoDeporte(req.getTipoDeporte());
        return v;
    }

    /**
     * Actualiza una entidad existente con campos del request (no sobrescribe audit fields ni relaciones no provistas).
     */
    public void updateEntityFromRequest(Venue target, VenueRequestDTO req, Owner owner) {
        if (target == null || req == null) return;
        if (req.getNombre() != null) target.setName(req.getNombre());
        if (req.getDireccion() != null) target.setAddress(req.getDireccion());
        if (req.getTimeZone() != null) target.setTimeZone(req.getTimeZone());
        if (req.getPhotos() != null) target.setPhotos(req.getPhotos());
        if (owner != null) target.setOwner(owner);
        // tipoDeporte: ver comentario en toEntity
    }
}
