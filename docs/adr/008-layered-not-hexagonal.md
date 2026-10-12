# ADR-008: Layered, not hexagonal

**Status:** accepted

## Context

One HTTP API comes in. One Mongo database is the store. A change to a filter should be a change to the query.

## Decision

Packages are `api` → `service` → `domain` / `repo`. Ingest writes through `repo`. REST uses records. Panache entities stop at `service`.

Hexagonal ports lose here because there is nothing for a port to switch. `RestaurantStore` would have one implementation. The request would follow the same path it follows now. Extract that interface when CDC is a second writer into the same model and HTTP create is no longer the only one. Until then a port is a type we would only mock.

The resource does not call Panache. The JSON must not be the Mongo entity, and a new filter must land in `RestaurantQueryService` rather than in the resource. `LayerRulesTest` fails the build if `api` reaches `repo`. The test records the rule. It is not the reason for the rule.

`service` imports `api.dto` on purpose. A second set of records would mirror the create and list shapes for a single API. The cost is that the service is tied to those HTTP types and is not reusable without them. That is accepted while there is one API.

ArchUnit also keeps `domain` free of `api`, `service`, `repo`, and `ingest`, and keeps `repo` and `ingest` free of `api`.

## Alternatives

- **Hexagonal ports now:** the textbook split, and one implementation behind each port. Rejected because it does not change behaviour. Revisit when a second writer exists.
- **Resource talks to Panache:** the fewest classes. Rejected because the HTTP type and the stored type become the same class, and every filter edit touches the resource.
- **A dto module the service does not import from `api`:** stricter layering. Rejected as a second copy of the same fields. The shortcut stays until a second caller needs different types.

## Consequences

A new filter such as `minVotes` is a query parameter and a condition in `RestaurantQueryService`. There is no port to register.

## Follow-up

This is layered on purpose. The split that has two sides is the document model and the CDC boundary (ADR-002, ADR-005), not a folder of ports with one adapter each.
