package com.apex.biteexpress.discovery.service;

import java.util.ArrayList;
import java.util.List;

public class RestaurantQuery {

  public String name;
  public String city;
  public List<String> cuisines = new ArrayList<>();
  public Double minRating;
  public Integer maxCostForTwo;
  public Double lat;
  public Double lng;
  public Double maxDistanceKm;
  public String text;
  public SortBy sort;
  public int page = 1;
  public int size = 20;

  public boolean hasCoordinates() {
    return lat != null && lng != null;
  }
}
