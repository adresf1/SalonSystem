package com.example.salon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private ServiceResponse service;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String customerName;
    private String customerPhone;
    private String status;
    private LocalDateTime createdAt;
}
