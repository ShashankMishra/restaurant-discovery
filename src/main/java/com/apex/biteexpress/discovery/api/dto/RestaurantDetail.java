package com.apex.biteexpress.discovery.api.dto;

import java.util.List;

public record RestaurantDetail(
    Long id,
    String name,
    String address,
    String locality,
    String city,
    Integer countryCode,
    String country,
    List<String> cuisines,
    Integer avgCostForTwo,
    String currency,
    Integer priceRange,
    Boolean tableBooking,
    Boolean onlineDelivery,
    Boolean deliveringNow,
    Double averageRating,
    Integer votes,
    String ratingText,
    Double latitude,
    Double longitude) {}
