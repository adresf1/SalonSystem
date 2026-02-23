package com.example.salon.controller;


import com.example.salon.dto.*;
import com.example.salon.service.BusinessHoursService;
import com.example.salon.service.BusinessOwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for BUSINESS_OWNER rolle
 * Frisører bruger disse endpoints til at administrere deres business
 */
@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BUSINESS_OWNER')")  // Kun BUSINESS_OWNER
public class BusinessOwnerController {

    private final BusinessOwnerService businessOwnerService;
    private final BusinessHoursService businessHoursService;

    // ============================================
    // BUSINESS INFO
    // ============================================

    /**
     * Get MY business info
     * GET /api/business/my-business
     */
    @GetMapping("/my-business")
    public ResponseEntity<BusinessResponse> getMyBusiness(Authentication authentication) {
        String username = authentication.getName();
        BusinessResponse business = businessOwnerService.getMyBusiness(username);
        return ResponseEntity.ok(business);
    }

    // ============================================
    // SERVICE MANAGEMENT
    // ============================================

    /**
     * Get MY services
     * GET /api/business/services
     */
    @GetMapping("/services")
    public ResponseEntity<List<ServiceResponse>> getMyServices(Authentication authentication) {
        String username = authentication.getName();
        List<ServiceResponse> services = businessOwnerService.getMyServices(username);
        return ResponseEntity.ok(services);
    }

    /**
     * Add new service to MY business
     * POST /api/business/services
     */
    @PostMapping("/services")
    public ResponseEntity<ServiceResponse> addService(
            Authentication authentication,
            @Valid @RequestBody ServiceRequest request) {
        String username = authentication.getName();
        ServiceResponse service = businessOwnerService.addService(username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(service);
    }

    /**
     * Update MY service
     * PUT /api/business/services/{serviceId}
     */
    @PutMapping("/services/{serviceId}")
    public ResponseEntity<ServiceResponse> updateService(
            Authentication authentication,
            @PathVariable Long serviceId,
            @Valid @RequestBody ServiceRequest request) {
        String username = authentication.getName();
        ServiceResponse service = businessOwnerService.updateService(username, serviceId, request);
        return ResponseEntity.ok(service);
    }

    /**
     * Delete MY service
     * DELETE /api/business/services/{serviceId}
     */
    @DeleteMapping("/services/{serviceId}")
    public ResponseEntity<Void> deleteService(
            Authentication authentication,
            @PathVariable Long serviceId) {
        String username = authentication.getName();
        businessOwnerService.deleteService(username, serviceId);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // BOOKING MANAGEMENT
    // ============================================

    /**
     * Get ALL MY bookings
     * GET /api/business/bookings
     */
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getAllMyBookings(Authentication authentication) {
        String username = authentication.getName();
        List<BookingResponse> bookings = businessOwnerService.getAllMyBookings(username);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get MY bookings for specific date
     * GET /api/business/bookings/date?date=2025-01-20
     */
    @GetMapping("/bookings/date")
    public ResponseEntity<List<BookingResponse>> getMyBookingsByDate(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String username = authentication.getName();
        List<BookingResponse> bookings = businessOwnerService.getMyBookingsByDate(username, date);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get TODAY's bookings
     * GET /api/business/bookings/today
     */
    @GetMapping("/bookings/today")
    public ResponseEntity<List<BookingResponse>> getTodayBookings(Authentication authentication) {
        String username = authentication.getName();
        List<BookingResponse> bookings = businessOwnerService.getMyBookingsByDate(
                username,
                LocalDate.now()
        );
        return ResponseEntity.ok(bookings);
    }

    /**
     * Mark booking as COMPLETED
     * PATCH /api/business/bookings/{bookingId}/complete
     */
    @PatchMapping("/bookings/{bookingId}/complete")
    public ResponseEntity<BookingResponse> completeBooking(
            Authentication authentication,
            @PathVariable Long bookingId) {
        String username = authentication.getName();
        BookingResponse booking = businessOwnerService.completeBooking(username, bookingId);
        return ResponseEntity.ok(booking);
    }

    /**
     * Cancel booking
     * PATCH /api/business/bookings/{bookingId}/cancel
     */
    @PatchMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            Authentication authentication,
            @PathVariable Long bookingId) {
        String username = authentication.getName();
        BookingResponse booking = businessOwnerService.cancelBooking(username, bookingId);
        return ResponseEntity.ok(booking);
    }

    // ============================================
    // BUSINESS HOURS MANAGEMENT
    // ============================================

    /**
     * Get business hours for all days
     * GET /api/business/hours
     */
    @GetMapping("/hours")
    public ResponseEntity<List<BusinessHoursDto>> getBusinessHours(Authentication authentication) {
        String username = authentication.getName();
        BusinessResponse business = businessOwnerService.getMyBusiness(username);
        List<BusinessHoursDto> hours = businessHoursService.getBusinessHours(business.getId());
        return ResponseEntity.ok(hours);
    }

    /**
     * Update business hours for a specific day
     * PUT /api/business/hours/{dayOfWeek}
     */
    @PutMapping("/hours/{dayOfWeek}")
    public ResponseEntity<BusinessHoursDto> updateBusinessHours(
            Authentication authentication,
            @PathVariable DayOfWeek dayOfWeek,
            @Valid @RequestBody BusinessHoursDto request) {
        String username = authentication.getName();
        BusinessResponse business = businessOwnerService.getMyBusiness(username);
        BusinessHoursDto hours = businessHoursService.updateBusinessHours(business.getId(), dayOfWeek, request);
        return ResponseEntity.ok(hours);
    }

    /**
     * Get closed dates (holidays, vacation, etc.)
     * GET /api/business/closed-dates
     */
    @GetMapping("/closed-dates")
    public ResponseEntity<List<ClosedDateDto>> getClosedDates(Authentication authentication) {
        String username = authentication.getName();
        BusinessResponse business = businessOwnerService.getMyBusiness(username);
        List<ClosedDateDto> closedDates = businessHoursService.getClosedDates(business.getId());
        return ResponseEntity.ok(closedDates);
    }

    /**
     * Add a closed date
     * POST /api/business/closed-dates
     */
    @PostMapping("/closed-dates")
    public ResponseEntity<ClosedDateDto> addClosedDate(
            Authentication authentication,
            @Valid @RequestBody ClosedDateDto request) {
        String username = authentication.getName();
        BusinessResponse business = businessOwnerService.getMyBusiness(username);
        ClosedDateDto closedDate = businessHoursService.addClosedDate(business.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(closedDate);
    }

    /**
     * Delete a closed date
     * DELETE /api/business/closed-dates/{closedDateId}
     */
    @DeleteMapping("/closed-dates/{closedDateId}")
    public ResponseEntity<Void> deleteClosedDate(@PathVariable Long closedDateId) {
        businessHoursService.deleteClosedDate(closedDateId);
        return ResponseEntity.noContent().build();
    }
}
