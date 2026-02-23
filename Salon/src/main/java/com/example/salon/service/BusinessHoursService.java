package com.example.salon.service;

import com.example.salon.dto.BusinessHoursDto;
import com.example.salon.dto.ClosedDateDto;
import com.example.salon.exception.ResourceNotFoundException;
import com.example.salon.model.Business;
import com.example.salon.model.BusinessHours;
import com.example.salon.model.ClosedDate;
import com.example.salon.repository.BusinessHoursRepository;
import com.example.salon.repository.BusinessRepository;
import com.example.salon.repository.ClosedDateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessHoursService {

    private final BusinessHoursRepository businessHoursRepository;
    private final ClosedDateRepository closedDateRepository;
    private final BusinessRepository businessRepository;

    @Transactional(readOnly = true)
    public List<BusinessHoursDto> getBusinessHours(Long businessId) {
        return businessHoursRepository.findByBusinessIdOrderByDayOfWeek(businessId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BusinessHoursDto updateBusinessHours(Long businessId, DayOfWeek dayOfWeek, BusinessHoursDto dto) {
        try {
            log.info("Updating business hours for business {} on {}: isOpen={}", businessId, dayOfWeek, dto.getIsOpen());
            
            Business business = businessRepository.findById(businessId)
                    .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

            BusinessHours hours = businessHoursRepository
                    .findByBusinessIdAndDayOfWeek(businessId, dayOfWeek)
                    .orElseGet(() -> BusinessHours.builder()
                            .business(business)
                            .dayOfWeek(dayOfWeek)
                            .build());

            hours.setIsOpen(dto.getIsOpen());
            hours.setOpenTime(dto.getOpenTime());
            hours.setCloseTime(dto.getCloseTime());
            hours.setBreakStartTime(dto.getBreakStartTime());
            hours.setBreakEndTime(dto.getBreakEndTime());

            hours = businessHoursRepository.save(hours);
            log.info("Successfully updated business hours for business {} on {}", businessId, dayOfWeek);

            return mapToDto(hours);
        } catch (Exception e) {
            log.error("Error updating business hours for business {} on {}: {}", businessId, dayOfWeek, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void initializeDefaultHours(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        // Check if hours already exist
        if (!businessHoursRepository.findByBusinessIdOrderByDayOfWeek(businessId).isEmpty()) {
            return;
        }

        // Create default hours: Monday-Friday 9:00-18:00, closed weekends
        for (DayOfWeek day : DayOfWeek.values()) {
            boolean isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
            
            BusinessHours hours = BusinessHours.builder()
                    .business(business)
                    .dayOfWeek(day)
                    .isOpen(isWeekday)
                    .openTime(isWeekday ? java.time.LocalTime.of(9, 0) : null)
                    .closeTime(isWeekday ? java.time.LocalTime.of(18, 0) : null)
                    .build();

            businessHoursRepository.save(hours);
        }

        log.info("Initialized default business hours for business {}", businessId);
    }

    // ============================================
    // CLOSED DATES
    // ============================================

    @Transactional(readOnly = true)
    public List<ClosedDateDto> getClosedDates(Long businessId) {
        LocalDate today = LocalDate.now();
        return closedDateRepository.findByBusinessIdAndClosedDateGreaterThanEqual(businessId, today)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClosedDateDto addClosedDate(Long businessId, ClosedDateDto dto) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));

        ClosedDate closedDate = ClosedDate.builder()
                .business(business)
                .closedDate(dto.getClosedDate())
                .reason(dto.getReason())
                .build();

        closedDate = closedDateRepository.save(closedDate);
        log.info("Added closed date {} for business {}", dto.getClosedDate(), businessId);

        return mapToDto(closedDate);
    }

    @Transactional
    public void deleteClosedDate(Long closedDateId) {
        closedDateRepository.deleteById(closedDateId);
        log.info("Deleted closed date {}", closedDateId);
    }

    public boolean isBusinessOpen(Long businessId, LocalDate date) {
        // Check if the date is explicitly closed
        if (closedDateRepository.findByBusinessIdAndClosedDate(businessId, date).isPresent()) {
            return false;
        }

        // Check day of week hours
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return businessHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek)
                .map(BusinessHours::getIsOpen)
                .orElse(false);
    }

    public BusinessHours getHoursForDay(Long businessId, DayOfWeek dayOfWeek) {
        return businessHoursRepository.findByBusinessIdAndDayOfWeek(businessId, dayOfWeek)
                .orElse(null);
    }

    // ============================================
    // MAPPERS
    // ============================================

    private BusinessHoursDto mapToDto(BusinessHours hours) {
        return BusinessHoursDto.builder()
                .id(hours.getId())
                .dayOfWeek(hours.getDayOfWeek())
                .isOpen(hours.getIsOpen())
                .openTime(hours.getOpenTime())
                .closeTime(hours.getCloseTime())
                .breakStartTime(hours.getBreakStartTime())
                .breakEndTime(hours.getBreakEndTime())
                .build();
    }

    private ClosedDateDto mapToDto(ClosedDate closedDate) {
        return ClosedDateDto.builder()
                .id(closedDate.getId())
                .closedDate(closedDate.getClosedDate())
                .reason(closedDate.getReason())
                .build();
    }
}
