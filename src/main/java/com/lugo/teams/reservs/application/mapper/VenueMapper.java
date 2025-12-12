// src/main/java/com/lugo/teams/reservs/application/mapper/VenueMapper.java
package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.owner.OwnerSummaryDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueListDTO;
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

    public VenueResponseDTO toResponseDTO(Venue v) {
        if (v == null) return null;
        VenueResponseDTO dto = VenueResponseDTO.builder()
                .id(v.getId())
                .name(v.getName())
                .address(v.getAddress())
                .timeZone(v.getTimeZone())
                .mainPhotoUrl(v.getMainPhotoUrl())
                .lat(v.getLat())
                .lng(v.getLng())
                .active(v.isActive())
                .photos(v.getPhotos() != null ? v.getPhotos() : Collections.emptyList())
                .allowOnsitePayment(v.isAllowOnsitePayment())
                .allowBankTransfer(v.isAllowBankTransfer())
                .allowOnlinePayment(v.isAllowOnlinePayment())
                .fieldsCount(v.getFields() != null ? v.getFields().size() : 0)
                .build();

        if (v.getOwner() != null) {
            OwnerSummaryDTO o = new OwnerSummaryDTO();
            o.setId(v.getOwner().getId());
            o.setName(v.getOwner().getName());
            dto.setOwner(o);
        }
        // fields list mapping (optional) - avoid N+1 in service/repo
        if (v.getFields() != null && !v.getFields().isEmpty()) {
            dto.setFields(v.getFields().stream().map(field -> {
                com.lugo.teams.reservs.application.dto.field.FieldSummaryDTO f = new com.lugo.teams.reservs.application.dto.field.FieldSummaryDTO();
                f.setId(field.getId());
                f.setName(field.getName());
                f.setCapacityPlayers(field.getCapacityPlayers());
                f.setPricePerHour(field.getPricePerHour());
                f.setFirstPhoto(field.getPhotos() != null && !field.getPhotos().isEmpty() ? field.getPhotos().get(0) : null);
                f.setSlotMinutes(field.getSlotMinutes());
                return f;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    public List<VenueListDTO> toResponseDTOList(List<Venue> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(v -> VenueListDTO.builder()
                .id(v.getId())
                .name(v.getName())
                .mainPhotoUrl(v.getMainPhotoUrl())
                .address(v.getAddress())
                .lat(v.getLat())
                .lng(v.getLng())
                .active(v.isActive())
                .allowOnsitePayment(v.isAllowOnsitePayment())
                .allowBankTransfer(v.isAllowBankTransfer())
                .allowOnlinePayment(v.isAllowOnlinePayment())
                .fieldsCount(v.getFields() != null ? v.getFields().size() : 0)
                .build()).collect(Collectors.toList());
    }

    public Venue toEntity(VenueRequestDTO req, Owner owner) {
        if (req == null) return null;
        Venue v = new Venue();
        v.setName(req.getName());
        v.setAddress(req.getAddress());
        v.setTimeZone(req.getTimeZone());
        v.setMainPhotoUrl(req.getMainPhotoUrl());
        v.setLat(req.getLat());
        v.setLng(req.getLng());
        v.setActive(req.getActive() != null ? req.getActive() : true);
        v.setPhotos(req.getPhotos() != null ? req.getPhotos() : Collections.emptyList());
        v.setOwner(owner);
        if (req.getAllowOnsitePayment() != null) v.setAllowOnsitePayment(req.getAllowOnsitePayment());
        if (req.getAllowBankTransfer() != null) v.setAllowBankTransfer(req.getAllowBankTransfer());
        if (req.getAllowOnlinePayment() != null) v.setAllowOnlinePayment(req.getAllowOnlinePayment());
        return v;
    }

    public void updateEntityFromRequest(Venue target, VenueRequestDTO req, Owner owner) {
        if (target == null || req == null) return;
        if (req.getName() != null) target.setName(req.getName());
        if (req.getAddress() != null) target.setAddress(req.getAddress());
        if (req.getTimeZone() != null) target.setTimeZone(req.getTimeZone());
        if (req.getMainPhotoUrl() != null) target.setMainPhotoUrl(req.getMainPhotoUrl());
        if (req.getLat() != null) target.setLat(req.getLat());
        if (req.getLng() != null) target.setLng(req.getLng());
        if (req.getPhotos() != null) target.setPhotos(req.getPhotos());
        if (req.getActive() != null) target.setActive(req.getActive());
        if (req.getAllowOnsitePayment() != null) target.setAllowOnsitePayment(req.getAllowOnsitePayment());
        if (req.getAllowBankTransfer() != null) target.setAllowBankTransfer(req.getAllowBankTransfer());
        if (req.getAllowOnlinePayment() != null) target.setAllowOnlinePayment(req.getAllowOnlinePayment());
        if (owner != null) target.setOwner(owner);
    }
}
