package com.example.salon.service;

import com.example.salon.dto.BookingResponse;
import com.example.salon.dto.BusinessResponse;
import com.example.salon.dto.ServiceRequest;
import com.example.salon.dto.ServiceResponse;
import com.example.salon.exception.ResourceNotFoundException;
import com.example.salon.exception.UnauthorizedException;
import com.example.salon.model.Booking;
import com.example.salon.model.Business;
import com.example.salon.model.Service;
import com.example.salon.model.User;
import com.example.salon.repository.BookingRepository;
import com.example.salon.repository.BusinessRepository;
import com.example.salon.repository.ServiceRepository;
import com.example.salon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class BusinessOwnerService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;

    @Value("${app.public.url:http://localhost:8081}")
    private String publicUrl;

    // ============================================
    // BUSINESS INFO
    // ============================================

    @Transactional(readOnly = true)
    public BusinessResponse getMyBusiness(String username) {
        User owner = getUserByUsername(username);
        Business business = getBusinessByOwner(owner);

        return BusinessResponse.builder()
                .id(business.getId())
                .name(business.getName())
                .slug(business.getSlug())
                .active(business.getActive())
                .bookingUrl(publicUrl + "/book/" + business.getSlug())
                .createdAt(business.getCreatedAt())
                .build();
    }

    // ============================================
    // SERVICE MANAGEMENT
    // ============================================

    @Transactional(readOnly = true)
    public List<ServiceResponse> getMyServices(String username) {
        User owner = getUserByUsername(username);
        Business business = getBusinessByOwner(owner);

        return serviceRepository.findByBusinessIdAndActiveTrue(business.getId())
                .stream()
                .map(this::mapServiceToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceResponse addService(String username, ServiceRequest request) {
        User owner = getUserByUsername(username);
        Business business = getBusinessByOwner(owner);

        Service service = Service.builder()
                .business(business)
                .name(request.getName())
                .durationMinutes(request.getDurationMinutes())
                .price(request.getPrice())
                .active(true)
                .build();

        service = serviceRepository.save(service);
        log.info("Service added by {}: {} for business: {}",
                username, service.getName(), business.getName());

        return mapServiceToResponse(service);
    }

    @Transactional
    public ServiceResponse updateService(String username, Long serviceId, ServiceRequest request) {
        User owner = getUserByUsername(username);
        Business business = getBusinessByOwner(owner);

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        // Verify ownership
        if (!service.getBusiness().getId().equals(business.getId())) {
            throw new UnauthorizedException("You don't have permission to update this service");
        }

        service.setName(request.getName());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setPrice(request.getPrice());

        service = serviceRepository.save(service);
        log.info("Service updated by {}: {}", username, service.getName());

        return mapServiceToResponse(service);
    }

    @Transactional
    public void deleteService(String username, Long serviceId) {
        User owner = getUserByUsername(username);
        Business business = getBusinessByOwner(owner);

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        // Verify ownership
        if (!service.getBusiness().getId().equals(business.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this service");
        }

        serviceRepository.delete(service);
        log.info("Service deleted by {}: {}", username, service.getName());
    }

    // ============================================
    // BOOKING MANAGEMENT
    // ============================================

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllMyBookings(String username) {
        User owner = getUserByUsername(username);
        Business business = getBusinessByOwner(owner);

        List<Booking> bookings = bookingRepository.findByBusinessId(business.getId());

        return bookings.stream()
                .map(this::mapBookingToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookingsByDate(String username, LocalDate date) {
        User owner = getUserByUsername(username);
        Business business = getBusinessByOwner(owner);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return bookingRepository
                .findByBusinessIdAndStartTimeBetween(business.getId(), startOfDay, endOfDay)
                .stream()
                .map(this::mapBookingToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse completeBooking(String username, Long bookingId) {
        User owner = getUserByUsername(username);
        Business business = getBusinessByOwner(owner);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Verify ownership
        if (!booking.getBusiness().getId().equals(business.getId())) {
            throw new UnauthorizedException("You don't have permission to modify this booking");
        }

        booking.setStatus(Booking.BookingStatus.COMPLETED);
        booking = bookingRepository.save(booking);

        log.info("Booking {} marked as completed by {}", bookingId, username);

        return mapBookingToResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(String username, Long bookingId) {
        User owner = getUserByUsername(username);
        Business business = getBusinessByOwner(owner);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Verify ownership
        if (!booking.getBusiness().getId().equals(business.getId())) {
            throw new UnauthorizedException("You don't have permission to cancel this booking");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);

        log.info("Booking {} cancelled by {}", bookingId, username);

        return mapBookingToResponse(booking);
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Business getBusinessByOwner(User owner) {
        if (owner.getBusiness() == null) {
            throw new ResourceNotFoundException("User is not associated with any business");
        }
        return owner.getBusiness();
    }

    private ServiceResponse mapServiceToResponse(Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .active(service.getActive())
                .build();
    }

    private BookingResponse mapBookingToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .service(mapServiceToResponse(booking.getService()))
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .customerName(booking.getCustomerName())
                .customerPhone(booking.getCustomerPhone())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
