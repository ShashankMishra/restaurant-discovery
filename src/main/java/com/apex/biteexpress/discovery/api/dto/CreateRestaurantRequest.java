package com.apex.biteexpress.discovery.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateRestaurantRequest(
    Long id,
    @NotBlank @Size(max = 200) String name,
    String address,
    String locality,
    @NotBlank String city,
    Integer countryCode,
    List<String> cuisines,
    Integer avgCostForTwo,
    String currency,
    Integer priceRange,
    Boolean tableBooking,
    Boolean onlineDelivery,
    Boolean deliveringNow,
    @DecimalMin("0.0") @DecimalMax("5.0") Double averageRating,
    Integer votes,
    String ratingText,
    @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
    @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {}
