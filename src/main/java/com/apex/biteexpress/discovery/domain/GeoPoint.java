package com.apex.biteexpress.discovery.domain;

import java.util.List;

/** GeoJSON Point. Coordinates are [longitude, latitude]. */
public class GeoPoint {

  public String type = "Point";
  public List<Double> coordinates;

  public GeoPoint() {}

  public GeoPoint(double longitude, double latitude) {
    this.coordinates = List.of(longitude, latitude);
  }

  public double longitude() {
    return coordinates.get(0);
  }

  public double latitude() {
    return coordinates.get(1);
  }
}
