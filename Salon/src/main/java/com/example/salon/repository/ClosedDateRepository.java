package com.example.salon.repository;

import com.example.salon.model.ClosedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClosedDateRepository extends JpaRepository<ClosedDate, Long> {
    
    List<ClosedDate> findByBusinessIdOrderByClosedDate(Long businessId);
    
    Optional<ClosedDate> findByBusinessIdAndClosedDate(Long businessId, LocalDate date);
    
    List<ClosedDate> findByBusinessIdAndClosedDateGreaterThanEqual(Long businessId, LocalDate fromDate);
}
