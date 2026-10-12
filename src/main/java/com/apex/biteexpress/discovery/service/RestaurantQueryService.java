package com.apex.biteexpress.discovery.service;

import com.apex.biteexpress.discovery.api.dto.RestaurantPage;
import com.apex.biteexpress.discovery.api.dto.RestaurantSummary;
import com.apex.biteexpress.discovery.repo.RestaurantRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.Document;

@ApplicationScoped
public class RestaurantQueryService {

  @Inject RestaurantRepository restaurants;
  @Inject RestaurantMapper mapper;

  public RestaurantPage search(RestaurantQuery q) {
    int page = Math.max(q.page, 1);
    int size = Math.min(Math.max(q.size, 1), 100);
    int skip = (page - 1) * size;

    Document filter = filterOf(q);
    List<Document> pipeline = new ArrayList<>();

    if (q.hasCoordinates()) {
      Document geoNear = new Document();
      geoNear.put(
          "near", new Document("type", "Point").append("coordinates", List.of(q.lng, q.lat)));
      geoNear.put("distanceField", "distanceMeters");
      geoNear.put("spherical", true);
      geoNear.put("query", filter);
      if (q.maxDistanceKm != null) {
        geoNear.put("maxDistance", q.maxDistanceKm * 1000.0);
      }
      pipeline.add(new Document("$geoNear", geoNear));
    } else {
      pipeline.add(new Document("$match", filter));
    }

    pipeline.add(new Document("$sort", sortDoc(q)));
    pipeline.add(new Document("$skip", skip));
    pipeline.add(new Document("$limit", size));

    List<RestaurantSummary> items =
        restaurants.aggregate(pipeline).stream().map(mapper::toSummary).toList();
    long total = restaurants.countDocuments(filter);
    return new RestaurantPage(items, page, size, total);
  }

  Document filterOf(RestaurantQuery q) {
    List<Document> and = new ArrayList<>();
    if (q.city != null && !q.city.isBlank()) {
      and.add(regex("city", q.city.trim(), true));
    }
    if (q.name != null && !q.name.isBlank()) {
      and.add(regex("name", q.name.trim(), false));
    }
    if (q.minRating != null) {
      and.add(new Document("rating.aggregate", new Document("$gte", q.minRating)));
    }
    if (q.maxCostForTwo != null) {
      and.add(new Document("avgCostForTwo", new Document("$lte", q.maxCostForTwo)));
    }
    for (String cuisine : q.cuisines) {
      if (cuisine != null && !cuisine.isBlank()) {
        and.add(new Document("cuisines", regexValue(cuisine.trim(), false)));
      }
    }
    // $text cannot share a pipeline with $geoNear; fall back to a name/cuisine regex.
    if (q.text != null && !q.text.isBlank()) {
      if (q.hasCoordinates()) {
        and.add(
            new Document(
                "$or",
                List.of(
                    regex("name", q.text, false),
                    new Document("cuisines", regexValue(q.text, false)))));
      } else {
        and.add(new Document("$text", new Document("$search", q.text)));
      }
    }
    if (and.isEmpty()) {
      return new Document();
    }
    if (and.size() == 1) {
      return and.get(0);
    }
    return new Document("$and", and);
  }

  private Document sortDoc(RestaurantQuery q) {
    SortBy sort = q.sort;
    if (sort == null) {
      sort = q.hasCoordinates() ? SortBy.DISTANCE : SortBy.RATING;
    }
    return switch (sort) {
      case DISTANCE -> new Document("distanceMeters", 1);
      case NAME -> new Document("nameLower", 1);
      case RATING -> new Document("rating.aggregate", -1).append("nameLower", 1);
    };
  }

  private static Document regex(String field, String value, boolean anchored) {
    return new Document(field, regexValue(value, anchored));
  }

  private static Document regexValue(String value, boolean anchored) {
    String body = Pattern.quote(value);
    String pattern = anchored ? "^" + body + "$" : body;
    return new Document("$regex", pattern).append("$options", "i");
  }
}
