package com.apex.biteexpress.discovery.ingest;

import com.apex.biteexpress.discovery.config.CountryNames;
import com.apex.biteexpress.discovery.domain.GeoPoint;
import com.apex.biteexpress.discovery.domain.Rating;
import com.apex.biteexpress.discovery.domain.Restaurant;
import com.apex.biteexpress.discovery.domain.RestaurantFlags;
import com.apex.biteexpress.discovery.repo.RestaurantRepository;
import com.apex.biteexpress.discovery.service.RestaurantMapper;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ZomatoCsvLoader {

  private static final Logger LOG = Logger.getLogger(ZomatoCsvLoader.class);

  @Inject RestaurantRepository restaurants;

  @ConfigProperty(name = "discovery.ingest.enabled", defaultValue = "true")
  boolean enabled;

  @ConfigProperty(name = "discovery.ingest.resource", defaultValue = "data/zomato.csv")
  String resourcePath;

  void onStart(@Observes @Priority(10) StartupEvent event) {
    if (!enabled) {
      return;
    }
    if (restaurants.count() > 0) {
      LOG.infof("skipping ingest; %d restaurants already present", restaurants.count());
      return;
    }
    int n = load();
    LOG.infof("ingested %d restaurants from %s", n, resourcePath);
  }

  int load() {
    InputStream in =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
    if (in == null) {
      throw new IllegalStateException("CSV not found on classpath: " + resourcePath);
    }
    List<Restaurant> batch = new ArrayList<>();
    try (InputStreamReader reader = new InputStreamReader(in, charset());
        CSVParser parser =
            CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .get()
                .parse(reader)) {
      for (CSVRecord row : parser) {
        Restaurant r = toRestaurant(row);
        if (r != null) {
          batch.add(r);
        }
      }
    } catch (Exception e) {
      throw new IllegalStateException("failed to parse " + resourcePath, e);
    }
    if (!batch.isEmpty()) {
      restaurants.persist(batch);
      long maxId = batch.stream().mapToLong(r -> r.id).max().orElse(0L);
      restaurants.bumpCounterToAtLeast(maxId);
    }
    return batch.size();
  }

  private Restaurant toRestaurant(CSVRecord row) {
    String idRaw = first(row, "Restaurant ID", "id");
    if (idRaw == null || idRaw.isBlank()) {
      return null;
    }
    Restaurant r = new Restaurant();
    r.id = Long.parseLong(idRaw.trim());
    r.name = first(row, "Restaurant Name", "name");
    if (r.name == null || r.name.isBlank()) {
      return null;
    }
    r.nameLower = r.name.toLowerCase();
    r.address = first(row, "Address", "address");
    r.locality = first(row, "Locality", "locality");
    r.city = first(row, "City", "city");
    r.countryCode = parseInt(first(row, "Country Code", "countryCode"));
    r.country = CountryNames.of(r.countryCode);
    r.cuisines = splitCuisines(first(row, "Cuisines", "cuisines"));
    r.avgCostForTwo = parseInt(first(row, "Average Cost for two", "avgCostForTwo"));
    r.currency = first(row, "Currency", "currency");
    r.priceRange = parseInt(first(row, "Price range", "priceRange"));
    r.flags =
        new RestaurantFlags(
            yes(first(row, "Has Table booking")),
            yes(first(row, "Has Online delivery")),
            yes(first(row, "Is delivering now")));
    r.rating =
        new Rating(
            parseDouble(first(row, "Aggregate rating", "averageRating")),
            parseInt(first(row, "Votes", "votes")),
            first(row, "Rating text", "ratingText"));
    Double lng = parseDouble(first(row, "Longitude", "longitude"));
    Double lat = parseDouble(first(row, "Latitude", "latitude"));
    if (lng != null && lat != null && RestaurantMapper.isValidCoord(lng, lat)) {
      r.location = new GeoPoint(lng, lat);
    }
    Instant now = Instant.now();
    r.createdAt = now;
    r.updatedAt = now;
    return r;
  }

  private static List<String> splitCuisines(String raw) {
    if (raw == null || raw.isBlank()) {
      return List.of();
    }
    return Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
  }

  private static boolean yes(String raw) {
    return raw != null && raw.equalsIgnoreCase("Yes");
  }

  private static String first(CSVRecord row, String... headers) {
    for (String h : headers) {
      if (row.isMapped(h)) {
        String v = row.get(h);
        if (v != null && !v.isBlank()) {
          return v;
        }
      }
    }
    return null;
  }

  private static Integer parseInt(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return (int) Double.parseDouble(raw.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Double parseDouble(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      return Double.parseDouble(raw.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static Charset charset() {
    // Kaggle dump is often Latin-1; bundled sample is UTF-8. ISO-8859-1 reads both.
    return StandardCharsets.ISO_8859_1;
  }
}
