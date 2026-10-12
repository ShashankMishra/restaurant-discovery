package com.apex.biteexpress.discovery.service;

public enum SortBy {
  RATING,
  DISTANCE,
  NAME;

  public static SortBy fromParam(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    return switch (raw.trim().toLowerCase()) {
      case "rating" -> RATING;
      case "distance" -> DISTANCE;
      case "name" -> NAME;
      default -> throw new IllegalArgumentException("sort must be rating, distance, or name");
    };
  }
}
