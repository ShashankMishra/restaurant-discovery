# ADR-008: Layered, not hexagonal

**Status:** accepted

## Context

One HTTP API, one Mongo database, a follow-up that wants small live edits.

## Decision

Packages: `api` → `service` → `domain` / `repo`. `ingest` is another adapter into `repo`. REST maps to records; Panache entities do not leave `service`.

ArchUnit: `api` must not touch `repo`; `domain` must not depend on `api`/`service`/`repo`; `repo`/`ingest` must not depend on `api`.

`service` *does* depend on `api.dto`. That is a known shortcut so I didn't add a second DTO module.

## Alternatives

- **Hexagonal / ports:** I would extract `RestaurantStore` when CDC is a second inbound adapter. Doing it now is an interface with one impl.
- **Resource talks to Panache:** fastest to type, ugly to test, and the first live edit will leak Mongo types into JSON.

## Consequences

Adding `minVotes` is a query-param plus a filter in `RestaurantQueryService`. No port wiring.

## Follow-up

"Isn't this just MVC?" Yes, on purpose. The interesting architecture is the strangler and the document model, not a hexagonal folder tree with one adapter.
