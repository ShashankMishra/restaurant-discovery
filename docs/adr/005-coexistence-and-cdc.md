# ADR-005: Coexistence with the monolith

**Status:** accepted (docs only in the MVP)

## Context

The partner portal already writes restaurants. Some of those fields sit next to payments. Discovery can be a few seconds behind. An order must not depend on this API.

## Decision

The RDBMS stays the source of record. A change-data capture stream (Debezium, or the same idea) upserts into Mongo on the restaurant id. This API is a read model.

That is the choice. The portal keeps writing where it writes today. A failed apply to Mongo retries from the log offset. It does not fail the portal transaction, and it does not fail an order.

This process still accepts `POST /v1/restaurants` straight into Mongo. That is so a restaurant can be created here with no monolith attached. It is not the production write path.

Dual-write, portal to both databases in one request, is the fallback only if the lag is too long. Writing Mongo first and the RDBMS second is not that fallback. The monolith still owns the flags that sit next to payments.

## Alternatives

- **Big-bang cutover:** one release moves the portal, the reports, and this API. It loses because a bad cut has nowhere to read from. CDC leaves the old write path serving, with discovery stale rather than down.
- **Mongo as source of record now:** one database, no lag. It loses because every current writer, including the portal, has to move before the new database is safe. That is a programme of its own. This service does not take it on.
- **Dual-write from the first day:** both stores update inside the portal request. It loses because either write can fail after the other has succeeded, and the portal has to know about Mongo. CDC keeps that failure in the stream. Discovery lags. Orders stay on the monolith. Use dual-write only when that lag is the problem we are solving.

## Consequences

`_id` in ADR-002 is the source id so the upsert key already exists. A delete or update that never streams leaves this API serving a restaurant the portal has already removed.

## Follow-up

RDBMS commit succeeded, Mongo apply failed: retry from the CDC offset, not from a thread in this API. Discovery may be stale for seconds. Orders still run on the monolith.
