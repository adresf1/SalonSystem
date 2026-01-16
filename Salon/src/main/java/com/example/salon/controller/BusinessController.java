package com.example.salon.controller;

import com.example.salon.dto.BusinessRequest;
import com.example.salon.dto.BusinessResponse;
import com.example.salon.dto.BusinessWithOwnerResponse;
import com.example.salon.service.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for SYSTEM_ADMIN rolle
 * Kun system admin kan oprette og administrere businesses
 */
@RestController
@RequestMapping("/api/admin/businesses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")  // Kun SYSTEM_ADMIN
public class BusinessController {

    private final BusinessService businessService;

    /**
     * Create new business WITH owner user
     * POST /api/admin/businesses
     *
     * Body:
     * {
     *   "name": "Premium Barber Shop",
     *   "slug": "premium-barber",
     *   "ownerEmail": "john@barber.dk"
     * }
     *
     * Response includes owner credentials to send via email
     */
    @PostMapping
    public ResponseEntity<BusinessWithOwnerResponse> createBusiness(
            @Valid @RequestBody BusinessRequest request) {
        BusinessWithOwnerResponse response = businessService.createBusiness(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all businesses
     * GET /api/admin/businesses
     */
    @GetMapping
    public ResponseEntity<List<BusinessResponse>> getAllBusinesses() {
        List<BusinessResponse> businesses = businessService.getAllBusinesses();
        return ResponseEntity.ok(businesses);
    }

    /**
     * Get business by slug
     * GET /api/admin/businesses/{slug}
     */
    @GetMapping("/{slug}")
    public ResponseEntity<BusinessResponse> getBusinessBySlug(@PathVariable String slug) {
        BusinessResponse business = businessService.getBusinessBySlug(slug);
        return ResponseEntity.ok(business);
    }

    /**
     * Update business status (active/inactive)
     * PATCH /api/admin/businesses/{id}/status?active=true
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<BusinessResponse> updateBusinessStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        BusinessResponse business = businessService.updateBusinessStatus(id, active);
        return ResponseEntity.ok(business);
    }

    /**
     * Delete business
     * DELETE /api/admin/businesses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBusiness(@PathVariable Long id) {
        businessService.deleteBusiness(id);
        return ResponseEntity.noContent().build();
    }
}

/*
ÆNDRINGER:
==========
1. ✅ @PreAuthorize("hasRole('SYSTEM_ADMIN')") - Kun system admin
2. ✅ createBusiness() returnerer nu BusinessWithOwnerResponse
   - Indeholder owner credentials
   - SYSTEM_ADMIN sender disse til owner

WORKFLOW:
=========
1. SYSTEM_ADMIN logger ind:
   POST /api/auth/login
   { "username": "system-admin", "password": "..." }

2. Opret business:
   POST /api/admin/businesses
   Authorization: Bearer {admin-token}
   {
     "name": "Premium Barber",
     "slug": "premium-barber",
     "ownerEmail": "john@barber.dk"
   }

3. Response:
   {
     "businessId": 1,
     "businessName": "Premium Barber",
     "businessSlug": "premium-barber",
     "bookingUrl": "http://localhost:8080/api/public/premium-barber",
     "ownerUsername": "premium-barber-owner",
     "ownerEmail": "john@barber.dk",
     "temporaryPassword": "Xk9#mP2$qR8L",
     "message": "Business created successfully! Send credentials to owner via email."
   }

4. SYSTEM_ADMIN sender email til john@barber.dk med credentials

5. Business owner logger ind:
   POST /api/auth/login
   { "username": "premium-barber-owner", "password": "Xk9#mP2$qR8L" }
*/