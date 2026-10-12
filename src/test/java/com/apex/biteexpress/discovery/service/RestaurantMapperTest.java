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
