package com.apex.biteexpress.discovery.service;

import com.apex.biteexpress.discovery.repo.RestaurantRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * City/cuisine names used by the NL parser. Seeds cover the spec examples even when Mongo is empty
 * (tests). After ingest we merge distinct values from the collection.
 */
@ApplicationScoped
public class Vocabulary {

  static final List<String> DEFAULT_CITIES =
      List.of(
          "Gurgaon",
          "Gurugram",
          "New Delhi",
          "Mumbai",
          "Bangalore",
          "Hyderabad",
          "Chennai",
          "Kolkata",
          "Pune",
          "Noida",
          "Faridabad");

  static final List<String> DEFAULT_CUISINES =
      List.of(
          "South Indian",
          "North Indian",
          "Vada Pav",
          "Chinese",
          "Fast Food",
          "Pizza",
          "Cafe",
          "Street Food",
          "Mughlai",
          "Continental",
          "Italian",
          "Bakery");

  @Inject RestaurantRepository restaurants;

  private volatile List<String> cities = DEFAULT_CITIES;
  private volatile List<String> cuisines = DEFAULT_CUISINES;

  void refresh(@Observes @Priority(20) StartupEvent event) {
    Set<String> citySet = new LinkedHashSet<>(DEFAULT_CITIES);
    Set<String> cuisineSet = new LinkedHashSet<>(DEFAULT_CUISINES);
    restaurants
        .listAll()
        .forEach(
            r -> {
              if (r.city != null && !r.city.isBlank()) {
                citySet.add(r.city);
              }
              if (r.cuisines != null) {
                r.cuisines.stream().filter(c -> c != null && !c.isBlank()).forEach(cuisineSet::add);
              }
            });
    cities = List.copyOf(citySet);
    cuisines = List.copyOf(cuisineSet);
  }

  public List<String> cities() {
    return cities;
  }

  public List<String> cuisines() {
    return cuisines;
  }

  public String findCity(String haystack) {
    return longestMatch(haystack, cities);
  }

  public String findCuisine(String haystack) {
    return longestMatch(haystack, cuisines);
  }

  static Vocabulary seedsOnly() {
    Vocabulary v = new Vocabulary();
    v.cities = DEFAULT_CITIES;
    v.cuisines = DEFAULT_CUISINES;
    return v;
  }

  static String longestMatch(String haystack, List<String> candidates) {
    String lower = haystack.toLowerCase(Locale.ROOT);
    String best = null;
    for (String c : candidates) {
      if (c != null && lower.contains(c.toLowerCase(Locale.ROOT))) {
        if (best == null || c.length() > best.length()) {
          best = c;
        }
      }
    }
    return best;
  }
}
