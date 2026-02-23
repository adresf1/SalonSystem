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
public class BusinessResponse {
    private Long id;
    private String name;
    private String slug;
    private Boolean active;
    private String bookingUrl;
    private LocalDateTime createdAt;
}