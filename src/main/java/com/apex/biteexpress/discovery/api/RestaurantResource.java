package com.apex.biteexpress.discovery.api;

import com.apex.biteexpress.discovery.api.dto.CreateRestaurantRequest;
import com.apex.biteexpress.discovery.api.dto.RestaurantDetail;
import com.apex.biteexpress.discovery.api.dto.RestaurantPage;
import com.apex.biteexpress.discovery.service.RestaurantQuery;
import com.apex.biteexpress.discovery.service.RestaurantService;
import com.apex.biteexpress.discovery.service.SortBy;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;

@Path("/v1/restaurants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RestaurantResource {

  @Inject RestaurantService restaurants;

  @POST
  @RunOnVirtualThread
  public Response create(@Valid CreateRestaurantRequest request) {
    RestaurantDetail created = restaurants.create(request);
    return Response.status(Response.Status.CREATED).entity(created).build();
  }

  @GET
  @Path("search")
  @RunOnVirtualThread
  public RestaurantPage search(
      @QueryParam("q") String q,
      @QueryParam("lat") Double lat,
      @QueryParam("lng") Double lng,
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("size") @DefaultValue("20") int size) {
    return restaurants.search(q, lat, lng, page, size);
  }

  @GET
  @Path("{id}")
  @RunOnVirtualThread
  public RestaurantDetail get(@PathParam("id") long id) {
    return restaurants.get(id);
  }

  @GET
  @RunOnVirtualThread
  public RestaurantPage list(
      @QueryParam("name") String name,
      @QueryParam("city") String city,
      @QueryParam("cuisines") String cuisines,
      @QueryParam("minRating") Double minRating,
      @QueryParam("lat") Double lat,
      @QueryParam("lng") Double lng,
      @QueryParam("maxDistanceKm") Double maxDistanceKm,
      @QueryParam("sort") String sort,
      @QueryParam("page") @DefaultValue("1") int page,
      @QueryParam("size") @DefaultValue("20") int size) {
    RestaurantQuery query = new RestaurantQuery();
    query.name = name;
    query.city = city;
    if (cuisines != null && !cuisines.isBlank()) {
      query.cuisines =
          Arrays.stream(cuisines.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
    query.minRating = minRating;
    query.lat = lat;
    query.lng = lng;
    query.maxDistanceKm = maxDistanceKm;
    query.sort = SortBy.fromParam(sort);
    query.page = page;
    query.size = size;
    return restaurants.list(query);
  }
}
