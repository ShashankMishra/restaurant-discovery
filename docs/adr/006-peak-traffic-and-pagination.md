# ADR-006: Peak reads and pagination

**Status:** accepted

## Context

Lunch in one city is a read spike. The API contract is `page` and `size`. One response has to stay bounded. There is no measured QPS, and Compose is one Mongo node, not a replica set.

## Decision

Pagination is skip/limit on `page` and `size`, with `size` capped at 100.

The other shape is a cursor (`rating` plus `_id`). A cursor is the better peak-traffic page: an insert does not shift rows, and a deep page does not walk everything it skips. It loses for this API because the client asks for a page number. "Page 7" is one skip. A cursor cannot jump there without walking the previous pages. Skip/limit matches the contract we have. The cost, which we are accepting, is that a large page number gets slower. The cap of 100 is what keeps a single response bounded while the catalogue is still small. Move to a cursor when offset time shows up, or when the client is infinite scroll and does not need page numbers.

Redis for the first page of a city is the same kind of "later". It wins once a page is measurably hot. It loses now because there is no miss rate to size it, and a CDC update would have to invalidate it. There is no such writer in this process yet. Until then a spike hits Mongo, which is the honest behaviour.

`readPreference=secondaryPreferred` on list waits for a replica set. List is the read-heavy call. The primary should keep ingest and writes. Compose has no secondary, so the preference would do nothing today.

Indexes for the city browse and for geo are ADR-003. They are what makes the skip cheap on the first pages. Pagination does not fix a collection scan.

## Alternatives

- **Cursor (`rating` + `_id`) now:** stable under inserts, cheap deep pages, no random page access. Rejected because this client sends `page`. Revisit when skip cost is measured, not before.
- **Cache the first page of each city:** right answer to a known hot key. Rejected until that key is measured and CDC can expire the entry. The cost of waiting is an uncached spike.

## Consequences

Deep skips get slower as the offset grows. The cap of 100 is enforced in `RestaurantQueryService`, not only written here.

## Follow-up

Capacity runs out first on a missing 2dsphere index, a cuisine regex that does not use the multikey index, or the single Mongo node. The application thread pool is not the limit while the work is this aggregation.
