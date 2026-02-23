package com.example.salon.repository;

import com.example.salon.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByBusinessIdAndActiveTrue(Long businessId);
    Optional<Service> findByIdAndBusinessId(Long id, Long businessId);
}