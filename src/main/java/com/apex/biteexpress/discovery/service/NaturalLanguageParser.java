package com.apex.biteexpress.discovery.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deterministic parser for the three spec examples plus a few close variants. Not trying to be a
 * general NLP stack — Atlas Search is the real next step.
 */
@ApplicationScoped
public class NaturalLanguageParser {

  private static final Pattern COST =
      Pattern.compile(
          "(?:under|below|less than)\\s*(?:rs\\.?|₹)?\\s*(\\d{2,6})", Pattern.CASE_INSENSITIVE);
  private static final Pattern IN_CITY =
      Pattern.compile("\\bin\\s+([A-Za-z][A-Za-z .]+)", Pattern.CASE_INSENSITIVE);

  private final Vocabulary vocabulary;

  @Inject
  public NaturalLanguageParser(Vocabulary vocabulary) {
    this.vocabulary = vocabulary;
  }

  public RestaurantQuery parse(String q, Double lat, Double lng) {
    if (q == null || q.isBlank()) {
      throw new IllegalArgumentException("q is required");
    }
    String raw = q.trim();
    String lower = raw.toLowerCase(Locale.ROOT);

    RestaurantQuery query = new RestaurantQuery();
    query.lat = lat;
    query.lng = lng;

    Matcher cost = COST.matcher(raw);
    if (cost.find()) {
      query.maxCostForTwo = Integer.parseInt(cost.group(1));
    }

    if (lower.contains("best")
        || lower.contains("top rated")
        || lower.contains("highest rated")
        || lower.contains("top restaurant")) {
      query.sort = SortBy.RATING;
      query.minRating = 4.0;
    }
    if (lower.contains("nearest") || lower.contains("nearby") || lower.contains("closest")) {
      query.sort = SortBy.DISTANCE;
    }

    query.city = vocabulary.findCity(raw);
    if (query.city == null) {
      Matcher in = IN_CITY.matcher(raw);
      if (in.find()) {
        query.city = in.group(1).replaceAll("(?i)\\s+with\\s+.*$", "").trim();
      }
    }

    String cuisine = vocabulary.findCuisine(raw);
    if (cuisine != null) {
      query.cuisines = new ArrayList<>(List.of(cuisine));
    } else {
      Matcher serves = Pattern.compile("serves\\s+(.+)$", Pattern.CASE_INSENSITIVE).matcher(raw);
      if (serves.find()) {
        query.cuisines = new ArrayList<>(List.of(serves.group(1).trim()));
      }
    }

    String leftover = stripKnown(raw, query);
    if (!leftover.isBlank()) {
      query.text = leftover;
    }
    return query;
  }

  private static String stripKnown(String raw, RestaurantQuery query) {
    String s = raw;
    s = COST.matcher(s).replaceAll(" ");
    s =
        s.replaceAll(
            "(?i)\\b(best|top rated|highest rated|restaurants?|places?|place|nearest|nearby|closest|that|with|food|for two|serves|under|below|less than|rs)\\b",
            " ");
    s = s.replaceAll("(?i)\\bin\\s+[A-Za-z][A-Za-z .]+", " ");
    if (query.city != null) {
      s = s.replaceAll("(?i)" + Pattern.quote(query.city), " ");
    }
    for (String c : query.cuisines) {
      s = s.replaceAll("(?i)" + Pattern.quote(c), " ");
    }
    return s.replaceAll("[^A-Za-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
  }
}
