package com.example.salon.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    @NotNull
    private Long serviceId;

    @NotNull
    @Future
    private LocalDateTime startTime;

    @NotBlank
    @Size(max = 100)
    private String customerName;

    @NotBlank
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Phone number must be 8-15 digits, optionally starting with +")
    private String customerPhone;
}