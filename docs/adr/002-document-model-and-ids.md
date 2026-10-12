# ADR-002: Restaurant document and `_id`

**Status:** accepted

## Context

The monolith already has a Restaurant ID. Discovery has to coexist with CDC.

## Decision

- One `restaurants` collection.
- `_id` = Zomato / source Restaurant ID (`Long`), not ObjectId.
- Embed `rating` and `flags`. Cuisines as an array. GeoJSON `Point` for coordinates.
- `nameLower` for sort. `countryCode` kept even though we also store `country`.
- POST without id uses `counters.restaurantId`.

Panache for persist/find. `MongoCollection.aggregate` for list/geo/`$text`.

## Alternatives

- **ObjectId `_id` plus `restaurantId` field:** extra index, extra join in the reviewer's head.
- **Normalized cuisines collection:** pointless at this size; `$in` on an array is the access pattern.

## Consequences

Idempotent upserts from CDC are `replaceOne({_id})`. GET `/v1/restaurants/{id}` matches the source system.

## Follow-up

Why not Atlas Search documents yet? Because the demo has to run on Docker Mongo 7 with no Atlas account.
