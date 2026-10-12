package com.apex.biteexpress.discovery.api;

import com.apex.biteexpress.discovery.api.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/** Quarkus REST prefers {@link ServerExceptionMapper} over a generic JAX-RS ExceptionMapper. */
public class ErrorMapper {

  @ServerExceptionMapper
  public Response constraint(ConstraintViolationException exception) {
    List<String> details =
        exception.getConstraintViolations().stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .toList();
    return json(400, "VALIDATION_ERROR", "request is not valid", details);
  }

  @ServerExceptionMapper
  public Response badRequest(IllegalArgumentException exception) {
    return json(400, "BAD_REQUEST", exception.getMessage(), List.of());
  }

  @ServerExceptionMapper
  public Response notFound(NotFoundException exception) {
    return json(404, "NOT_FOUND", exception.getMessage(), List.of());
  }

  @ServerExceptionMapper
  public Response http(WebApplicationException exception) {
    int status = exception.getResponse() == null ? 500 : exception.getResponse().getStatus();
    if (status == 404) {
      return json(404, "NOT_FOUND", exception.getMessage(), List.of());
    }
    String code = status == 409 ? "CONFLICT" : "HTTP_" + status;
    return json(status, code, exception.getMessage(), List.of());
  }

  @ServerExceptionMapper
  public Response fallback(Exception exception) {
    return json(500, "INTERNAL", "unexpected error", List.of());
  }

  private static Response json(int status, String code, String message, List<String> details) {
    return Response.status(status).entity(new ErrorResponse(code, message, details)).build();
  }
}
