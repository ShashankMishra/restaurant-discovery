package com.apex.biteexpress.discovery.domain;

import io.quarkus.mongodb.panache.common.MongoEntity;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.bson.codecs.pojo.annotations.BsonId;

@MongoEntity(collection = "restaurants")
public class Restaurant {

  @BsonId public Long id;
  public String name;
  public String nameLower;
  public GeoPoint location;
  public String address;
  public String locality;
  public String city;
  public Integer countryCode;
  public String country;
  public List<String> cuisines = new ArrayList<>();
  public Integer avgCostForTwo;
  public String currency;
  public Integer priceRange;
  public RestaurantFlags flags;
  public Rating rating;
  public Instant createdAt;
  public Instant updatedAt;
}
