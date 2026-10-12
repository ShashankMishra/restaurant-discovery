package com.apex.biteexpress.discovery.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import com.apex.biteexpress.discovery.domain.GeoPoint;
import com.apex.biteexpress.discovery.domain.Rating;
import com.apex.biteexpress.discovery.domain.Restaurant;
import com.apex.biteexpress.discovery.repo.RestaurantRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class RestaurantResourceTest {

  @Inject RestaurantRepository restaurants;

  private final AtomicLong ids = new AtomicLong(1_000);

  @BeforeEach
  void clean() {
    restaurants.deleteAll();
  }

  @Test
  void discoveryPageIsServed() {
    given().when().get("/").then().statusCode(200).contentType("text/html");
  }

  @Test
  void createThenGetById() {
    long id =
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {
                  "name": "Test Dosa House",
                  "city": "Gurgaon",
                  "address": "Sector 29",
                  "cuisines": ["South Indian"],
                  "avgCostForTwo": 800,
                  "averageRating": 4.2,
                  "votes": 12,
                  "ratingText": "Very Good",
                  "latitude": 28.4595,
                  "longitude": 77.0266
                }
                """)
            .when()
            .post("/v1/restaurants")
            .then()
            .statusCode(201)
            .body("name", equalTo("Test Dosa House"))
            .body("country", equalTo("India"))
            .body("averageRating", equalTo(4.2f))
            .body("votes", equalTo(12))
            .body("ratingText", equalTo("Very Good"))
            .body("avgCostForTwo", equalTo(800))
            .extract()
            .jsonPath()
            .getLong("id");

    given()
        .when()
        .get("/v1/restaurants/{id}", id)
        .then()
        .statusCode(200)
        .body("id", equalTo((int) id))
        .body("averageRating", equalTo(4.2f));
  }

  @Test
  void missingRestaurantIs404() {
    given()
        .when()
        .get("/v1/restaurants/{id}", 999999)
        .then()
        .statusCode(404)
        .body("code", equalTo("NOT_FOUND"));
  }

  @Test
  void createRejectsBlankName() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"\",\"city\":\"Gurgaon\"}")
        .when()
        .post("/v1/restaurants")
        .then()
        .statusCode(400)
        .body("code", equalTo("VALIDATION_ERROR"));
  }

  @Test
  void listFiltersByCityAndCuisine() {
    seed("Naivedyam", "Gurgaon", "South Indian", 4.4, 700, 28.4688, 77.0865);
    seed("Leopold Cafe", "Mumbai", "Continental", 4.1, 1800, 18.9220, 72.8331);

    given()
        .queryParam("city", "Gurgaon")
        .queryParam("cuisines", "South Indian")
        .when()
        .get("/v1/restaurants")
        .then()
        .statusCode(200)
        .body("total", equalTo(1))
        .body("items[0].name", equalTo("Naivedyam"))
        .body("items[0].city", equalTo("Gurgaon"))
        .body("items[0].avgCostForTwo", equalTo(700));
  }

  @Test
  void listFiltersByMaxCostForTwo() {
    seed("Cheap Thali House", "Gurgaon", "North Indian", 3.2, 400, 28.4560, 77.0298);
    seed("Barbeque Nation", "Gurgaon", "North Indian", 4.5, 2500, 28.4672, 77.0644);

    given()
        .queryParam("city", "Gurgaon")
        .queryParam("maxCostForTwo", 1000)
        .when()
        .get("/v1/restaurants")
        .then()
        .statusCode(200)
        .body("total", equalTo(1))
        .body("items[0].name", equalTo("Cheap Thali House"))
        .body("items[0].avgCostForTwo", equalTo(400))
        .body("items[0].currency", equalTo("Rs."));
  }

  @Test
  void geoListReturnsDistance() {
    seed("Near", "Gurgaon", "Vada Pav", 4.0, 250, 28.4595, 77.0266);
    seed("Far", "Gurgaon", "Vada Pav", 4.0, 250, 28.70, 77.20);

    given()
        .queryParam("lat", 28.4595)
        .queryParam("lng", 77.0266)
        .queryParam("sort", "distance")
        .queryParam("maxDistanceKm", 20)
        .when()
        .get("/v1/restaurants")
        .then()
        .statusCode(200)
        .body("items[0].name", equalTo("Near"))
        .body("items[0].distanceKm", notNullValue())
        .body("total", greaterThan(0));
  }

  @Test
  void distanceSortWithoutCoordsIs400() {
    given()
        .queryParam("sort", "distance")
        .when()
        .get("/v1/restaurants")
        .then()
        .statusCode(400)
        .body("code", equalTo("BAD_REQUEST"));
  }

  @Test
  void nlSearchMatchesSpecPhrases() {
    seed("Sagar Ratna", "Gurgaon", "South Indian", 4.2, 900, 28.4710, 77.0490);
    seed("Vada Pav Junction", "Gurgaon", "Vada Pav", 4.0, 250, 28.4595, 77.0689);
    seed("Barbeque Nation", "Gurgaon", "North Indian", 4.5, 2500, 28.4672, 77.0644);
    seed("Cheap Thali House", "Gurgaon", "North Indian", 3.2, 400, 28.4560, 77.0298);

    given()
        .queryParam("q", "Best restaurants in Gurgaon with South Indian Food")
        .when()
        .get("/v1/restaurants/search")
        .then()
        .statusCode(200)
        .body("items.name", hasItem("Sagar Ratna"));

    given()
        .queryParam("q", "Restaurants in Gurgaon under Rs. 1000 for two")
        .when()
        .get("/v1/restaurants/search")
        .then()
        .statusCode(200)
        .body("items.name", hasItem("Cheap Thali House"));

    given()
        .queryParam("q", "Nearest place that serves Vada Pav")
        .queryParam("lat", 28.4595)
        .queryParam("lng", 77.0266)
        .when()
        .get("/v1/restaurants/search")
        .then()
        .statusCode(200)
        .body("items[0].name", equalTo("Vada Pav Junction"));
  }

  private void seed(
      String name,
      String city,
      String cuisine,
      double rating,
      int costForTwo,
      double lat,
      double lng) {
    Restaurant restaurant = new Restaurant();
    restaurant.id = ids.incrementAndGet();
    restaurant.name = name;
    restaurant.nameLower = name.toLowerCase();
    restaurant.city = city;
    restaurant.address = "test";
    restaurant.countryCode = 1;
    restaurant.country = "India";
    restaurant.cuisines = List.of(cuisine);
    restaurant.avgCostForTwo = costForTwo;
    restaurant.currency = "Rs.";
    restaurant.rating = new Rating(rating, 10, null);
    restaurant.location = new GeoPoint(lng, lat);
    restaurants.persist(restaurant);
  }
}
