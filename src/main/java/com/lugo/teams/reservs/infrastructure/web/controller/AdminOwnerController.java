package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.superAdmin.OwnerCreateRequestDTO;
import com.lugo.teams.reservs.application.dto.superAdmin.OwnerResponseDTO;
import com.lugo.teams.reservs.application.service.AdminOwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/owners")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPERADMIN')")
public class AdminOwnerController {

    private final AdminOwnerService adminOwnerService;

    // AdminOwnerController.java (solo la anotación del método)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OwnerResponseDTO> createOwner(
            @Valid @RequestBody OwnerCreateRequestDTO dto,
            UriComponentsBuilder uriBuilder
    ) {
        OwnerResponseDTO created = adminOwnerService.createOwner(dto);
        URI location = uriBuilder.path("/api/admin/owners/{id}").buildAndExpand(created.getOwnerId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<OwnerResponseDTO>> findAll() {
        return ResponseEntity.ok(adminOwnerService.findAllOwners());
    }

    @GetMapping("/{ownerId}")
    public ResponseEntity<OwnerResponseDTO> findById(@PathVariable Long ownerId) {
        return ResponseEntity.ok(adminOwnerService.findById(ownerId));
    }

    @PatchMapping("/{ownerId}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long ownerId) {
        adminOwnerService.enableOwner(ownerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{ownerId}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long ownerId) {
        adminOwnerService.disableOwner(ownerId);
        return ResponseEntity.noContent().build();
    }
}
