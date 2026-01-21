package com.example.salon.service;

import com.example.salon.dto.AvailableTimeSlot;
import com.example.salon.dto.AvailableTimesResponse;
import com.example.salon.dto.BookingRequest;
import com.example.salon.dto.BookingResponse;
import com.example.salon.dto.ServiceResponse;
import com.example.salon.exception.BookingConflictException;
import com.example.salon.exception.BusinessNotActiveException;
import com.example.salon.exception.ResourceNotFoundException;
import com.example.salon.model.Booking;
import com.example.salon.model.Business;
import com.example.salon.model.Service;
import com.example.salon.repository.BookingRepository;
import com.example.salon.repository.BusinessRepository;
import com.example.salon.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BusinessRepository businessRepository;
    private final ServiceRepository serviceRepository;

    @Transactional
    public BookingResponse createBooking(String businessSlug, BookingRequest request) {
        // Find and validate business
        Business business = businessRepository.findBySlug(businessSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessSlug));

        if (!business.getActive()) {
            throw new BusinessNotActiveException("Business is not accepting bookings");
        }

        // Find and validate service
        Service service = serviceRepository.findByIdAndBusinessId(request.getServiceId(), business.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (!service.getActive()) {
            throw new ResourceNotFoundException("Service is not available");
        }

        // Calculate end time
        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        // Validate no past bookings
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BookingConflictException("Cannot book in the past");
        }

        // Check for overlapping bookings
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                business.getId(),
                startTime,
                endTime
        );

        if (!overlapping.isEmpty()) {
            throw new BookingConflictException(
                    "Time slot is not available. Booking conflicts with existing appointment."
            );
        }

        // Create booking
        Booking booking = Booking.builder()
                .business(business)
                .service(service)
                .startTime(startTime)
                .endTime(endTime)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .status(Booking.BookingStatus.CONFIRMED)
                .build();

        booking = bookingRepository.save(booking);

        log.info("Booking created: {} for business: {}", booking.getId(), businessSlug);

        return mapToBookingResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByDate(String businessSlug, LocalDateTime date) {
        Business business = businessRepository.findBySlug(businessSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessSlug));

        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return bookingRepository
                .findByBusinessIdAndStartTimeBetween(business.getId(), startOfDay, endOfDay)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getCustomerBookings(String phone) {
        return bookingRepository.findByCustomerPhone(phone)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        log.info("Booking cancelled: {}", bookingId);
    }

    @Transactional(readOnly = true)
    public AvailableTimesResponse getAvailableTimeSlots(String businessSlug, LocalDate date, Long serviceId) {
        // Find and validate business
        Business business = businessRepository.findBySlug(businessSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessSlug));

        if (!business.getActive()) {
            throw new BusinessNotActiveException("Business is not accepting bookings");
        }

        // Find and validate service
        Service service = serviceRepository.findByIdAndBusinessId(serviceId, business.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        if (!service.getActive()) {
            throw new ResourceNotFoundException("Service is not available");
        }

        // Get all bookings for this day
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Booking> existingBookings = bookingRepository
                .findByBusinessIdAndStartTimeBetween(business.getId(), startOfDay, endOfDay);

        // Generate time slots (9:00 - 18:00 in 30 minute intervals)
        List<AvailableTimeSlot> timeSlots = new ArrayList<>();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(18, 0);
        int intervalMinutes = 30;

        while (startTime.isBefore(endTime)) {
            LocalDateTime slotStart = LocalDateTime.of(date, startTime);
            LocalDateTime slotEnd = slotStart.plusMinutes(service.getDurationMinutes());

            // Check if this slot conflicts with any existing booking
            boolean isAvailable = !hasConflict(slotStart, slotEnd, existingBookings);

            // Don't allow booking in the past
            if (slotStart.isBefore(LocalDateTime.now())) {
                isAvailable = false;
            }

            timeSlots.add(AvailableTimeSlot.builder()
                    .startTime(slotStart)
                    .endTime(slotEnd)
                    .available(isAvailable)
                    .build());

            startTime = startTime.plusMinutes(intervalMinutes);
        }

        return AvailableTimesResponse.builder()
                .date(date)
                .timeSlots(timeSlots)
                .build();
    }

    private boolean hasConflict(LocalDateTime slotStart, LocalDateTime slotEnd, List<Booking> existingBookings) {
        for (Booking booking : existingBookings) {
            // Skip cancelled bookings
            if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
                continue;
            }

            // Check if there's any overlap
            if (slotStart.isBefore(booking.getEndTime()) && slotEnd.isAfter(booking.getStartTime())) {
                return true;
            }
        }
        return false;
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .service(ServiceResponse.builder()
                        .id(booking.getService().getId())
                        .name(booking.getService().getName())
                        .durationMinutes(booking.getService().getDurationMinutes())
                        .price(booking.getService().getPrice())
                        .active(booking.getService().getActive())
                        .build())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .customerName(booking.getCustomerName())
                .customerPhone(booking.getCustomerPhone())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
