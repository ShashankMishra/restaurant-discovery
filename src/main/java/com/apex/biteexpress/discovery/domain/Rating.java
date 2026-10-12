package com.apex.biteexpress.discovery.domain;

public class Rating {

  public Double aggregate;
  public Integer votes;
  public String text;

  public Rating() {}

  public Rating(Double aggregate, Integer votes, String text) {
    this.aggregate = aggregate;
    this.votes = votes;
    this.text = text;
  }
}
