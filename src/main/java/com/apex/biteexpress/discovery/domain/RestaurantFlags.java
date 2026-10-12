package com.apex.biteexpress.discovery.domain;

public class RestaurantFlags {

  public boolean tableBooking;
  public boolean onlineDelivery;
  public boolean deliveringNow;

  public RestaurantFlags() {}

  public RestaurantFlags(boolean tableBooking, boolean onlineDelivery, boolean deliveringNow) {
    this.tableBooking = tableBooking;
    this.onlineDelivery = onlineDelivery;
    this.deliveringNow = deliveringNow;
  }
}
