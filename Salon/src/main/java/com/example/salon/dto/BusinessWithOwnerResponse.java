package com.example.salon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessWithOwnerResponse {

    // Business info
    private Long businessId;
    private String businessName;
    private String businessSlug;
    private String bookingUrl;

    // Owner credentials (sendes til SYSTEM_ADMIN)
    private String ownerUsername;
    private String ownerEmail;
    private String temporaryPassword;

    // Message
    private String message;
}
