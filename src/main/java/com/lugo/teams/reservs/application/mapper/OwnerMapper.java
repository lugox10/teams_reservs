package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.owner.OwnerRequestDTO;
import com.lugo.teams.reservs.application.dto.owner.OwnerResponseDTO;
import com.lugo.teams.reservs.application.dto.owner.OwnerSummaryDTO;
import com.lugo.teams.reservs.domain.model.Owner;
import org.springframework.stereotype.Component;

@Component
public class OwnerMapper {

    public OwnerResponseDTO toResponseDTO(Owner e) {
        if (e == null) return null;
        return OwnerResponseDTO.builder()
                .id(e.getId())
                .nombre(e.getName())              // DTO usa 'nombre'
                .telefono(e.getPhone())           // DTO usa 'telefono'
                .email(e.getEmail())
                .address(e.getAddress())          // si tu Owner tiene address; si no, omitir
                .build();
    }

    public OwnerSummaryDTO toSummaryDTO(Owner e) {
        if (e == null) return null;
        OwnerSummaryDTO s = new OwnerSummaryDTO();
        s.setId(e.getId());
        s.setNombre(e.getName());
        return s;
    }

    public Owner toEntity(OwnerRequestDTO req) {
        if (req == null) return null;
        Owner o = new Owner();
        o.setName(req.getNombre());
        o.setEmail(req.getEmail());
        o.setPhone(req.getTelefono());     // OwnerRequestDTO tiene 'telefono' en tu snapshot
        o.setAddress(req.getAddress());    // si existe en DTO
        return o;
    }

    public void updateEntityFromRequest(Owner target, OwnerRequestDTO req) {
        if (target == null || req == null) return;
        if (req.getNombre() != null) target.setName(req.getNombre());
        if (req.getEmail() != null) target.setEmail(req.getEmail());
        if (req.getTelefono() != null) target.setPhone(req.getTelefono());
        if (req.getAddress() != null) target.setAddress(req.getAddress());
    }
}
