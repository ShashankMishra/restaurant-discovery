package com.apex.biteexpress.discovery.service;

import com.apex.biteexpress.discovery.api.dto.CreateRestaurantRequest;
import com.apex.biteexpress.discovery.api.dto.RestaurantDetail;
import com.apex.biteexpress.discovery.domain.Restaurant;
import com.apex.biteexpress.discovery.repo.RestaurantRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class RestaurantService {

  @Inject RestaurantRepository restaurants;
  @Inject RestaurantMapper mapper;

  public RestaurantDetail create(CreateRestaurantRequest req) {
    long id = req.id() == null ? restaurants.nextId() : req.id();
    if (restaurants.findByIdOptional(id).isPresent()) {
      throw new WebApplicationException(
          "restaurant id already exists: " + id, Response.Status.CONFLICT);
    }
    Restaurant saved = mapper.fromCreate(req, id);
    restaurants.persist(saved);
    return mapper.toDetail(saved);
  }

  public RestaurantDetail get(long id) {
    Restaurant r =
        restaurants
            .findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("restaurant not found: " + id));
    return mapper.toDetail(r);
  }
}
