# Architecture

BiteExpress today: consumer apps, partner portal, rider apps, internal dashboards, all talking to a **multi-instance monolith** in Gurugram. Relational databases, some with sync between instances. Payments already sit with a third party.

Immediate pain (6–12 months) is peak-hour reliability. The 2–3 year story is cloud, more countries, and data-residency rules.

This repo is only **restaurant discovery**. Orders and payments stay on the monolith. That is a strangler slice, not a rewrite.

```
today:  apps -> monolith -> RDBMS (Gurugram)

mvp:    apps (or a BFF) -> Quarkus discovery API -> MongoDB 7

later:  RDBMS --CDC--> Atlas (Search + residency-aware clusters)
        discovery API stays; Mongo connection string changes
```

## Why this slice first

Discovery is read-heavy at lunch/dinner. If the list endpoint is slow, people bounce and you lose orders that the payments stack never sees. Putting restaurants in Mongo lets us use GeoJSON + a 2dsphere index without dragging the whole order schema along.

## Coexistence (6–12 months)

I would **not** make this API the system of record on day one.

- Keep RDBMS as SoR for restaurant master data.
- CDC (Debezium) into Mongo, upsert on the existing Restaurant ID (`_id`).
- Discovery reads Mongo. Writes from the partner portal still hit the monolith; Mongo follows.
- Dual-write only if CDC is too slow to land — then you need an outbox, because "RDBMS ok, Mongo failed" is how you get ghost restaurants.

Cutover: send a percentage of `GET /v1/restaurants*` to this service, compare counts vs the old list, then flip. POST on this API is for the POC and for net-new tenants later.

## Peak traffic

Covering indexes are in `MongoIndexCreator`. `$geoNear` is first in the pipeline. Skip/limit pagination is honest about offset cost — see [adr/006-peak-traffic-and-pagination.md](adr/006-peak-traffic-and-pagination.md).

## International / DPDP / GDPR

`countryCode` is already on the document so we can pin India data to an India cluster later. This collection is public restaurant catalogue, not diner PII. Consumer profiles should never land here.

## This codebase

Quarkus 3, Java 21, Gradle, layered packages (`api` / `service` / `domain` / `repo`). JVM container. Details in the ADRs.
