package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.field.FieldSummaryDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueListDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueRequestDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueResponseDTO;
import com.lugo.teams.reservs.application.dto.owner.OwnerSummaryDTO;
import com.lugo.teams.reservs.domain.model.Field;
import com.lugo.teams.reservs.domain.model.Owner;
import com.lugo.teams.reservs.domain.model.Venue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VenueMapper {

    public Venue toEntity(VenueRequestDTO dto, Owner owner) {
        if (dto == null) return null;
        Venue v = new Venue();

        v.setOwner(owner);
        v.setName(dto.getName());
        v.setAddress(dto.getAddress());
        v.setTimeZone(dto.getTimeZone());
        v.setMainPhotoUrl(dto.getMainPhotoUrl());
        v.setLat(dto.getLat());
        v.setLng(dto.getLng());

        if (dto.getActive() != null) v.setActive(dto.getActive());
        if (dto.getAllowOnsitePayment() != null) v.setAllowOnsitePayment(dto.getAllowOnsitePayment());
        if (dto.getAllowBankTransfer() != null) v.setAllowBankTransfer(dto.getAllowBankTransfer());
        if (dto.getAllowOnlinePayment() != null) v.setAllowOnlinePayment(dto.getAllowOnlinePayment());

        if (dto.getPhotos() != null) {
            v.setPhotos(new ArrayList<>(dto.getPhotos()));
        } else {
            v.setPhotos(new ArrayList<>());
        }

        return v;
    }

    public void updateEntityFromRequest(Venue existing, VenueRequestDTO dto, Owner ownerIfProvided) {
        if (existing == null || dto == null) return;

        if (ownerIfProvided != null) existing.setOwner(ownerIfProvided);

        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getAddress() != null) existing.setAddress(dto.getAddress());
        if (dto.getTimeZone() != null) existing.setTimeZone(dto.getTimeZone());
        if (dto.getMainPhotoUrl() != null) existing.setMainPhotoUrl(dto.getMainPhotoUrl());
        if (dto.getLat() != null) existing.setLat(dto.getLat());
        if (dto.getLng() != null) existing.setLng(dto.getLng());

        if (dto.getActive() != null) existing.setActive(dto.getActive());
        if (dto.getAllowOnsitePayment() != null) existing.setAllowOnsitePayment(dto.getAllowOnsitePayment());
        if (dto.getAllowBankTransfer() != null) existing.setAllowBankTransfer(dto.getAllowBankTransfer());
        if (dto.getAllowOnlinePayment() != null) existing.setAllowOnlinePayment(dto.getAllowOnlinePayment());

        if (dto.getPhotos() != null) {
            if (existing.getPhotos() == null) existing.setPhotos(new ArrayList<>());
            existing.getPhotos().clear();
            existing.getPhotos().addAll(dto.getPhotos());
        }
    }

    public VenueResponseDTO toResponseDTO(Venue v) {
        if (v == null) return null;

        VenueResponseDTO dto = VenueResponseDTO.builder()
                .id(v.getId())
                .owner(v.getOwner() != null ? toOwnerSummary(v.getOwner()) : null)
                .name(v.getName())
                .address(v.getAddress())
                .timeZone(v.getTimeZone())
                .mainPhotoUrl(v.getMainPhotoUrl())
                .lat(v.getLat())
                .lng(v.getLng())
                .active(v.isActive())
                .allowOnsitePayment(v.isAllowOnsitePayment())
                .allowBankTransfer(v.isAllowBankTransfer())
                .allowOnlinePayment(v.isAllowOnlinePayment())
                .photos(v.getPhotos() != null ? new ArrayList<>(v.getPhotos()) : new ArrayList<>())
                .fieldsCount(v.getFields() != null ? v.getFields().size() : 0)
                .fields(v.getFields() != null
                        ? v.getFields().stream().map(this::toFieldSummary).collect(Collectors.toList())
                        : new ArrayList<>())
                .build();

        return dto;
    }

    // Resumen / lista (VenueListDTO)
    public List<VenueListDTO> toListDTO(List<Venue> list) {
        if (list == null) return List.of();
        return list.stream().map(this::toListItem).collect(Collectors.toList());
    }

    // Compatibilidad con nombres previos en servicios
    public List<VenueListDTO> toResponseDTOList(List<Venue> list) {
        return toListDTO(list);
    }

    private VenueListDTO toListItem(Venue v) {
        VenueListDTO l = new VenueListDTO();
        l.setId(v.getId());
        l.setName(v.getName());
        l.setMainPhotoUrl(v.getMainPhotoUrl());
        l.setAddress(v.getAddress());
        l.setLat(v.getLat());
        l.setLng(v.getLng());
        l.setActive(v.isActive());
        l.setAllowOnsitePayment(v.isAllowOnsitePayment());
        l.setAllowBankTransfer(v.isAllowBankTransfer());
        l.setAllowOnlinePayment(v.isAllowOnlinePayment());
        l.setFieldsCount(v.getFields() != null ? v.getFields().size() : 0);
        return l;
    }

    private FieldSummaryDTO toFieldSummary(Field f) {
        FieldSummaryDTO fs = new FieldSummaryDTO();
        if (f == null) return fs;

        // Id / name
        fs.setId(f.getId());
        fs.setName(f.getName());

        // Venue name (para listados rápidos)
        fs.setVenueName(f.getVenue() != null ? f.getVenue().getName() : null);

        // Capacity / price / slotMinutes
        fs.setCapacityPlayers(f.getCapacityPlayers());
        fs.setPricePerHour(f.getPricePerHour());
        fs.setSlotMinutes(f.getSlotMinutes());

        // firstPhoto: la primera si existe
        String firstPhoto = null;
        if (f.getPhotos() != null && !f.getPhotos().isEmpty()) firstPhoto = f.getPhotos().get(0);
        fs.setFirstPhoto(firstPhoto);

        return fs;
    }

    private OwnerSummaryDTO toOwnerSummary(Owner o) {
        OwnerSummaryDTO os = new OwnerSummaryDTO();
        if (o == null) return os;
        os.setId(o.getId());
        os.setName(o.getName());
        return os;
    }
}
