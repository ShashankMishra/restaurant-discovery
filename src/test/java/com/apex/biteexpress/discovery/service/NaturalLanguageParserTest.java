package com.apex.biteexpress.discovery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NaturalLanguageParserTest {

  private NaturalLanguageParser parser;

  @BeforeEach
  void setUp() {
    parser = new NaturalLanguageParser(Vocabulary.seedsOnly());
  }

  @Test
  void bestSouthIndianInGurgaon() {
    RestaurantQuery q =
        parser.parse("Best restaurants in Gurgaon with South Indian Food", null, null);
    assertEquals("Gurgaon", q.city);
    assertEquals(SortBy.RATING, q.sort);
    assertEquals(4.0, q.minRating);
    assertTrue(q.cuisines.stream().anyMatch(c -> c.equalsIgnoreCase("South Indian")));
  }

  @Test
  void underThousandInGurgaon() {
    RestaurantQuery q = parser.parse("Restaurants in Gurgaon under Rs. 1000 for two", null, null);
    assertEquals("Gurgaon", q.city);
    assertEquals(1000, q.maxCostForTwo);
  }

  @Test
  void nearestVadaPavNeedsCoordsForDistanceQuery() {
    RestaurantQuery q = parser.parse("Nearest place that serves Vada Pav", 28.4595, 77.0266);
    assertEquals(SortBy.DISTANCE, q.sort);
    assertTrue(q.cuisines.stream().anyMatch(c -> c.toLowerCase().contains("vada pav")));
    assertEquals(28.4595, q.lat);
    assertEquals(77.0266, q.lng);
    assertTrue(q.text == null || q.text.isBlank());
  }

  @Test
  void blankQueryRejected() {
    assertThrows(IllegalArgumentException.class, () -> parser.parse("  ", null, null));
  }
}
