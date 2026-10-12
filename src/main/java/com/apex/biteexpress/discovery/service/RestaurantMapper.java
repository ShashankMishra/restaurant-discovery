package com.apex.biteexpress.discovery.service;

import com.apex.biteexpress.discovery.api.dto.CreateRestaurantRequest;
import com.apex.biteexpress.discovery.api.dto.RestaurantDetail;
import com.apex.biteexpress.discovery.api.dto.RestaurantSummary;
import com.apex.biteexpress.discovery.config.CountryNames;
import com.apex.biteexpress.discovery.domain.GeoPoint;
import com.apex.biteexpress.discovery.domain.Rating;
import com.apex.biteexpress.discovery.domain.Restaurant;
import com.apex.biteexpress.discovery.domain.RestaurantFlags;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import org.bson.Document;

@ApplicationScoped
public class RestaurantMapper {

  public Restaurant fromCreate(CreateRestaurantRequest req, long id) {
    Restaurant r = new Restaurant();
    r.id = id;
    r.name = req.name().trim();
    r.nameLower = r.name.toLowerCase();
    r.address = req.address();
    r.locality = req.locality();
    r.city = req.city().trim();
    r.countryCode = req.countryCode() == null ? 1 : req.countryCode();
    r.country = CountryNames.of(r.countryCode);
    r.cuisines = req.cuisines() == null ? List.of() : List.copyOf(req.cuisines());
    r.avgCostForTwo = req.avgCostForTwo();
    r.currency = req.currency() == null ? "Rs." : req.currency();
    r.priceRange = req.priceRange();
    r.flags =
        new RestaurantFlags(
            Boolean.TRUE.equals(req.tableBooking()),
            Boolean.TRUE.equals(req.onlineDelivery()),
            Boolean.TRUE.equals(req.deliveringNow()));
    String ratingText = req.ratingText() == null ? null : req.ratingText().trim();
    if (ratingText != null && ratingText.isEmpty()) {
      ratingText = null;
    }
    if (req.averageRating() != null || req.votes() != null || ratingText != null) {
      r.rating = new Rating(req.averageRating(), req.votes(), ratingText);
    }
    if (req.latitude() != null
        && req.longitude() != null
        && isValidCoord(req.longitude(), req.latitude())) {
      r.location = new GeoPoint(req.longitude(), req.latitude());
    }
    Instant now = Instant.now();
    r.createdAt = now;
    r.updatedAt = now;
    return r;
  }

  public RestaurantDetail toDetail(Restaurant r) {
    Double lat = r.location == null ? null : r.location.latitude();
    Double lng = r.location == null ? null : r.location.longitude();
    return new RestaurantDetail(
        r.id,
        r.name,
        r.address,
        r.locality,
        r.city,
        r.countryCode,
        r.country,
        r.cuisines,
        r.avgCostForTwo,
        r.currency,
        r.priceRange,
        r.flags == null ? null : r.flags.tableBooking,
        r.flags == null ? null : r.flags.onlineDelivery,
        r.flags == null ? null : r.flags.deliveringNow,
        r.rating == null ? null : r.rating.aggregate,
        r.rating == null ? null : r.rating.votes,
        r.rating == null ? null : r.rating.text,
        lat,
        lng);
  }

  public RestaurantSummary toSummary(Document d) {
    Double distanceKm = null;
    if (d.containsKey("distanceMeters") && d.get("distanceMeters") instanceof Number n) {
      distanceKm = Math.round(n.doubleValue() / 10.0) / 100.0;
    }
    Document rating = d.get("rating", Document.class);
    @SuppressWarnings("unchecked")
    List<String> cuisines = (List<String>) d.getOrDefault("cuisines", List.of());
    return new RestaurantSummary(
        asLong(d.get("_id")),
        d.getString("name"),
        d.getString("address"),
        d.getString("city"),
        d.getString("country"),
        cuisines,
        rating == null ? null : asDouble(rating.get("aggregate")),
        asInt(d.get("avgCostForTwo")),
        d.getString("currency"),
        distanceKm);
  }

  public static boolean isValidCoord(double lng, double lat) {
    if (lng == 0.0 && lat == 0.0) {
      return false;
    }
    return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
  }

  private static Long asLong(Object v) {
    return v instanceof Number n ? n.longValue() : null;
  }

  private static Double asDouble(Object v) {
    return v instanceof Number n ? n.doubleValue() : null;
  }

  private static Integer asInt(Object v) {
    return v instanceof Number n ? n.intValue() : null;
  }
}
