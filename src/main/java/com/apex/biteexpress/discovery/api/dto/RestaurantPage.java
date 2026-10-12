package com.apex.biteexpress.discovery.api.dto;

import java.util.List;

public record RestaurantPage(List<RestaurantSummary> items, int page, int size, long total) {}
