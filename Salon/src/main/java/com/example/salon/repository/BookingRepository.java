package com.example.salon.repository;

import com.example.salon.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.business.id = :businessId " +
            "AND b.status = 'CONFIRMED' " +
            "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    List<Booking> findOverlappingBookings(
            @Param("businessId") Long businessId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Booking> findByBusinessIdAndStartTimeBetween(
            Long businessId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    List<Booking> findByCustomerPhone(String phone);

    // ============================================
    // NY METODE: Find all bookings for a business
    // ============================================
    List<Booking> findByBusinessId(Long businessId);
}

/*
NY METODE:
==========
✅ findByBusinessId(Long businessId)
   - Returnerer ALLE bookings for et business
   - Bruges af BusinessOwnerService til at vise alle bookings
*/