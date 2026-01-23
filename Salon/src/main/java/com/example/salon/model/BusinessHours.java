package com.example.salon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "business_hours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "is_open", nullable = false)
    @Builder.Default
    private Boolean isOpen = false;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    // Constraint: If isOpen = true, openTime and closeTime must be set
    @PrePersist
    @PreUpdate
    protected void validate() {
        if (isOpen && (openTime == null || closeTime == null)) {
            throw new IllegalStateException("Open time and close time must be set when business is open");
        }
        if (!isOpen) {
            openTime = null;
            closeTime = null;
            breakStartTime = null;
            breakEndTime = null;
        }
    }
}
