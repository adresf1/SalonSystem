package com.example.salon.service;

import com.example.salon.dto.BusinessRequest;
import com.example.salon.dto.BusinessResponse;
import com.example.salon.dto.BusinessWithOwnerResponse;
import com.example.salon.exception.DuplicateResourceException;
import com.example.salon.exception.ResourceNotFoundException;
import com.example.salon.model.Business;
import com.example.salon.model.User;
import com.example.salon.repository.BusinessRepository;
import com.example.salon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessHoursService businessHoursService;

    @Value("${app.public.url:http://localhost:8081}")
    private String publicUrl;

    /**
     * Opretter et nyt business MED en owner user
     * SYSTEM_ADMIN kalder denne metode
     */
    @Transactional
    public BusinessWithOwnerResponse createBusiness(BusinessRequest request) {
        // Check if slug already exists
        if (businessRepository.existsBySlug(request.getSlug())) {
            throw new DuplicateResourceException("Business slug already exists: " + request.getSlug());
        }

        // Check if owner email already exists
        if (userRepository.existsByEmail(request.getOwnerEmail())) {
            throw new DuplicateResourceException("Owner email already exists: " + request.getOwnerEmail());
        }

        // Generate username from slug
        String ownerUsername = request.getSlug() + "-owner";

        if (userRepository.existsByUsername(ownerUsername)) {
            throw new DuplicateResourceException("Username already exists: " + ownerUsername);
        }

        // Generate temporary password
        String temporaryPassword = generateTemporaryPassword();

        // Step 1: Create Business (without owner first)
        Business business = Business.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .active(true)
                .build();

        business = businessRepository.save(business);

        // Step 2: Create Owner User
        User owner = User.builder()
                .username(ownerUsername)
                .email(request.getOwnerEmail())
                .password(passwordEncoder.encode(temporaryPassword))
                .role(User.Role.BUSINESS_OWNER)
                .business(business)  // ← Link to business
                .active(true)
                .build();

        owner = userRepository.save(owner);

        // Step 3: Update Business with owner
        business.setOwner(owner);
        business = businessRepository.save(business);

        // Step 4: Initialize default business hours (Monday-Friday 9:00-18:00)
        businessHoursService.initializeDefaultHours(business.getId());

        log.info("Business created: {} with owner: {}", business.getName(), owner.getUsername());

        // Return response with owner credentials
        return BusinessWithOwnerResponse.builder()
                .businessId(business.getId())
                .businessName(business.getName())
                .businessSlug(business.getSlug())
                .bookingUrl(publicUrl + "/book/" + business.getSlug())
                .ownerUsername(ownerUsername)
                .ownerEmail(request.getOwnerEmail())
                .temporaryPassword(temporaryPassword)
                .message("Business created successfully! Send credentials to owner via email.")
                .build();
    }

    @Transactional(readOnly = true)
    public BusinessResponse getBusinessBySlug(String slug) {
        Business business = businessRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + slug));

        return mapToResponse(business);
    }

    @Transactional(readOnly = true)
    public List<BusinessResponse> getAllBusinesses() {
        return businessRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BusinessResponse updateBusinessStatus(Long id, boolean active) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        business.setActive(active);
        business = businessRepository.save(business);

        log.info("Business {} status updated to: {}", business.getName(), active);

        return mapToResponse(business);
    }

    @Transactional
    public void deleteBusiness(Long id) {
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        businessRepository.delete(business);

        log.info("Business deleted: {}", business.getName());
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private String generateTemporaryPassword() {
        String chars = "123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private BusinessResponse mapToResponse(Business business) {
        return BusinessResponse.builder()
                .id(business.getId())
                .name(business.getName())
                .slug(business.getSlug())
                .active(business.getActive())
                .bookingUrl(publicUrl + "/book/" + business.getSlug())
                .createdAt(business.getCreatedAt())
                .build();
    }
}