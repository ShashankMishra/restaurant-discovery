package com.apex.biteexpress.discovery.api.dto;

import java.util.List;

public record RestaurantSummary(
    Long id,
    String name,
    String address,
    String city,
    String country,
    List<String> cuisines,
    Double averageRating,
    Double distanceKm) {}
