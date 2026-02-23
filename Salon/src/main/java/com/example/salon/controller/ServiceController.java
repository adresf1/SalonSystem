package com.example.salon.controller;


import com.example.salon.dto.ServiceRequest;
import com.example.salon.dto.ServiceResponse;
import com.example.salon.service.ServiceManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/businesses/{businessSlug}/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceManagementService serviceManagementService;

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(
            @PathVariable String businessSlug,
            @Valid @RequestBody ServiceRequest request) {
        ServiceResponse service = serviceManagementService.createService(businessSlug, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(service);
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getServices(@PathVariable String businessSlug) {
        List<ServiceResponse> services = serviceManagementService.getActiveServices(businessSlug);
        return ResponseEntity.ok(services);
    }
}