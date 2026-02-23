package com.example.salon.service;

import com.example.salon.dto.ServiceRequest;
import com.example.salon.dto.ServiceResponse;
import com.example.salon.exception.ResourceNotFoundException;
import com.example.salon.model.Business;
import com.example.salon.model.Service;
import com.example.salon.repository.BusinessRepository;
import com.example.salon.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;

    @Transactional
    public ServiceResponse createService(String businessSlug, ServiceRequest request) {
        Business business = businessRepository.findBySlug(businessSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessSlug));

        Service service = Service.builder()
                .business(business)
                .name(request.getName())
                .durationMinutes(request.getDurationMinutes())
                .price(request.getPrice())
                .active(true)
                .build();

        service = serviceRepository.save(service);

        log.info("Service created: {} for business: {}", service.getName(), businessSlug);

        return mapToResponse(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getActiveServices(String businessSlug) {
        Business business = businessRepository.findBySlug(businessSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessSlug));

        return serviceRepository.findByBusinessIdAndActiveTrue(business.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ServiceResponse updateService(String businessSlug, Long serviceId, ServiceRequest request) {
        Business business = businessRepository.findBySlug(businessSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessSlug));

        Service service = serviceRepository.findByIdAndBusinessId(serviceId, business.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        service.setName(request.getName());
        service.setDurationMinutes(request.getDurationMinutes());
        service.setPrice(request.getPrice());

        service = serviceRepository.save(service);

        log.info("Service updated: {}", service.getId());

        return mapToResponse(service);
    }

    @Transactional
    public void deleteService(String businessSlug, Long serviceId) {
        Business business = businessRepository.findBySlug(businessSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessSlug));

        Service service = serviceRepository.findByIdAndBusinessId(serviceId, business.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));

        serviceRepository.delete(service);

        log.info("Service deleted: {}", serviceId);
    }

    private ServiceResponse mapToResponse(Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .active(service.getActive())
                .build();
    }
}