package com.lugo.teams.reservs.application.mapper;
import com.lugo.teams.reservs.application.dto.owner.OwnerRequestDTO;
import com.lugo.teams.reservs.application.dto.owner.OwnerResponseDTO;
import com.lugo.teams.reservs.application.dto.owner.OwnerSummaryDTO;
import com.lugo.teams.reservs.domain.model.Owner;
import com.lugo.teams.reservs.domain.model.ReservUser;
import org.springframework.stereotype.Component;

@Component
public class OwnerMapper {

    /**
     * Entidad -> response DTO (incluye datos del ReservUser si está linkeado).
     */
    public OwnerResponseDTO toResponseDTO(Owner e) {
        if (e == null) return null;
        OwnerResponseDTO.OwnerResponseDTOBuilder b = OwnerResponseDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .address(e.getAddress())
                .nit(e.getNit())
                .businessName(e.getBusinessName())
                .logo(e.getLogo());

        if (e.getUser() != null) {
            ReservUser u = e.getUser();
            b.userId(u.getId());
            b.username(u.getUsername());
        }

        return b.build();
    }

    public OwnerSummaryDTO toSummaryDTO(Owner e) {
        if (e == null) return null;
        return OwnerSummaryDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .build();
    }

    /**
     * Crea una entidad Owner a partir del request.
     * - reservUser: opcional, si ya lo creaste en el service pásalo para ligarlo.
     * - No setea id ni auditing.
     */
    public Owner toEntity(OwnerRequestDTO req, ReservUser reservUser) {
        if (req == null) return null;
        Owner.OwnerBuilder b = Owner.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .address(req.getAddress())
                .nit(req.getNit())
                .businessName(req.getBusinessName())
                .logo(req.getLogo());

        Owner o = b.build();
        if (reservUser != null) o.setUser(reservUser);
        // NOTA: si el req incluye password/username: el service debe crear el ReservUser y pasar el objeto aquí.
        return o;
    }

    /**
     * Actualiza parcialmente la entidad Owner desde el request.
     * - Si pasas un ReservUser no nulo, lo liga (útil para operaciones admin).
     */
    public void updateEntityFromRequest(Owner target, OwnerRequestDTO req, ReservUser reservUser) {
        if (target == null || req == null) return;
        if (req.getName() != null) target.setName(req.getName());
        if (req.getEmail() != null) target.setEmail(req.getEmail());
        if (req.getPhone() != null) target.setPhone(req.getPhone());
        if (req.getAddress() != null) target.setAddress(req.getAddress());
        if (req.getNit() != null) target.setNit(req.getNit());
        if (req.getBusinessName() != null) target.setBusinessName(req.getBusinessName());
        if (req.getLogo() != null) target.setLogo(req.getLogo());
        if (reservUser != null) target.setUser(reservUser);
        // No tocamos password aquí: la auth principal (ReservUser) gestiona passwords.
    }
}
