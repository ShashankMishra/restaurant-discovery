package com.apex.biteexpress.discovery.api;

import com.apex.biteexpress.discovery.api.dto.CreateRestaurantRequest;
import com.apex.biteexpress.discovery.api.dto.RestaurantDetail;
import com.apex.biteexpress.discovery.service.RestaurantService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
  @Path("{id}")
  @RunOnVirtualThread
  public RestaurantDetail get(@PathParam("id") long id) {
    return restaurants.get(id);
  }
}
