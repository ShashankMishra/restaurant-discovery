# ADR-006: Peak reads and pagination

**Status:** accepted

## Context

Lunch in Gurgaon is a spike. Skip/limit is what every tutorial ships.

## Decision

MVP: `page`/`size` with skip/limit, max page size 100. Indexes aimed at the city+rating browse and geo.

Not in the JVM process: Redis. Not in Compose: extra secondaries. I would set `readPreference=secondaryPreferred` on list once there is a replica set.

## Alternatives

- **Cursor pagination (`_id` + rating):** the right peak-traffic shape. More awkward for "jump to page 7" in a mobile list. Next, not now.
- **Cache the first page of each city:** yes, after we see real QPS.

## Consequences

Deep offsets will get slower. I would rather say that than hide it.

## Follow-up

What actually falls over first: missing 2dsphere, collection scan on cuisine regex, or a single Mongo node. Not Quarkus thread count — virtual threads are for the narrative, not the bottleneck.
