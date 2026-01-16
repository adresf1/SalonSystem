package com.example.salon.controller;

import com.example.salon.dto.BookingRequest;
import com.example.salon.dto.BookingResponse;
import com.example.salon.dto.ServiceResponse;
import com.example.salon.service.BookingService;
import com.example.salon.service.ServiceManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/public/{businessSlug}")
@RequiredArgsConstructor
public class PublicBookingController {

    private final BookingService bookingService;
    private final ServiceManagementService serviceManagementService;

    @GetMapping("/services")
    public ResponseEntity<List<ServiceResponse>> getServices(@PathVariable String businessSlug) {
        List<ServiceResponse> services = serviceManagementService.getActiveServices(businessSlug);
        return ResponseEntity.ok(services);
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> createBooking(
            @PathVariable String businessSlug,
            @Valid @RequestBody BookingRequest request) {
        BookingResponse booking = bookingService.createBooking(businessSlug, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsByDate(
            @PathVariable String businessSlug,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        List<BookingResponse> bookings = bookingService.getBookingsByDate(businessSlug, date);
        return ResponseEntity.ok(bookings);
    }
}