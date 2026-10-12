package com.apex.biteexpress.discovery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.apex.biteexpress.discovery.api.dto.CreateRestaurantRequest;
import com.apex.biteexpress.discovery.api.dto.RestaurantDetail;
import com.apex.biteexpress.discovery.api.dto.RestaurantSummary;
import com.apex.biteexpress.discovery.domain.Restaurant;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.Test;

class RestaurantMapperTest {

  private final RestaurantMapper mapper = new RestaurantMapper();

  @Test
  void createMapsCountryAndDropsZeroCoords() {
    CreateRestaurantRequest req =
        new CreateRestaurantRequest(
            99L,
            " Test Kitchen ",
            "Sector 29",
            "Sector 29",
            "Gurgaon",
            1,
            List.of("Cafe"),
            800,
            "Rs.",
            2,
            true,
            true,
            false,
            null,
            null,
            null,
            0.0,
            0.0);
    Restaurant r = mapper.fromCreate(req, 99L);
    assertEquals("Test Kitchen", r.name);
    assertEquals("test kitchen", r.nameLower);
    assertEquals("India", r.country);
    assertNull(r.location);
    assertNull(r.rating);
    RestaurantDetail d = mapper.toDetail(r);
    assertEquals(99L, d.id());
    assertNull(d.latitude());
  }

  @Test
  void createStoresRatingSnapshot() {
    CreateRestaurantRequest req =
        new CreateRestaurantRequest(
            null,
            "Live Edit Kitchen",
            null,
            null,
            "Gurgaon",
            null,
            List.of("Cafe"),
            800,
            null,
            null,
            null,
            null,
            null,
            4.2,
            12,
            " Very Good ",
            28.46,
            77.03);
    Restaurant r = mapper.fromCreate(req, 50L);
    assertEquals(4.2, r.rating.aggregate);
    assertEquals(12, r.rating.votes);
    assertEquals("Very Good", r.rating.text);
    RestaurantDetail d = mapper.toDetail(r);
    assertEquals(4.2, d.averageRating());
    assertEquals(12, d.votes());
    assertEquals("Very Good", d.ratingText());
  }

  @Test
  void summaryReadsGeoDistance() {
    Document doc =
        new Document("_id", 1L)
            .append("name", "Naivedyam")
            .append("address", "DLF")
            .append("city", "Gurgaon")
            .append("country", "India")
            .append("cuisines", List.of("South Indian"))
            .append("rating", new Document("aggregate", 4.4))
            .append("avgCostForTwo", 700)
            .append("currency", "Rs.")
            .append("distanceMeters", 1234.0);
    RestaurantSummary s = mapper.toSummary(doc);
    assertEquals(1.23, s.distanceKm());
    assertEquals(4.4, s.averageRating());
    assertEquals(700, s.avgCostForTwo());
  }

  @Test
  void zeroZeroIsNotAPlace() {
    assertFalse(RestaurantMapper.isValidCoord(0, 0));
  }
}
