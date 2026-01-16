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
public class BusinessRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
    @Size(min = 3, max = 50)
    private String slug;

    // ============================================
    // NY: Owner email (SYSTEM_ADMIN angiver dette)
    // ============================================
    @NotBlank(message = "Owner email is required")
    @Email(message = "Must be a valid email")
    private String ownerEmail;
}
