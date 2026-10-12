# ADR-002: Restaurant document and `_id`

**Status:** accepted

## Context

List and detail read name, city, cuisines, rating, and location together. The monolith already has a Restaurant ID. Discovery has to accept CDC upserts on that id (ADR-005), and `GET /v1/restaurants/{id}` has to be the same number.

## Decision

`_id` is the source restaurant id (`Long`), not an ObjectId.

That is the choice that matters. An ObjectId plus a separate `restaurantId` field can hold the same document. It loses on the id itself: the URL id would not be `_id`, and a CDC upsert would need a second unique index instead of `replaceOne({_id})`. We give up letting Mongo mint the identity.

The other fields stay on the same document. List, detail, nearest, and the three search sentences all read the restaurant and its current facts together. A second collection is a `$lookup` on that path.

- **Rating is the aggregate, nested.** `rating.aggregate`, `votes`, and `text` are one snapshot per restaurant, which is what the CSV already is. List filters and sorts `rating.aggregate`. The city + rating index only works because that number is on this document. A `ratings` or `reviews` collection would join on every "best in Gurgaon". Add `reviews` only when a diner writes a review: many rows, their own writes, their own retention. Create and ingest copy the aggregate onto the restaurant. They do not open a review thread.
- **Flags stay nested.** `tableBooking`, `onlineDelivery`, and `deliveringNow` are three booleans on the card. `deliveringNow` changes more often than the name. It is still one field read with the restaurant, not its own document.
- **Location stays a GeoJSON point on the restaurant.** `$geoNear` needs it on the same document as the 2dsphere index. A coordinates collection cannot be that index.
- Cuisines stay an array on the restaurant.
- `nameLower` is stored for case-insensitive sort.
- Both `countryCode` and `country` are stored.
- `POST` with no id allocates from `counters.restaurantId`.

Panache for persist and find. `MongoCollection.aggregate` for list, geo, and `$text`, because those are pipelines rather than a single-document get.

## Alternatives

- **ObjectId `_id` plus `restaurantId`:** same payload, worse key. GET and CDC would both query the second field. The extra unique index is the cost of ignoring the source id. We give up Mongo's own id generation on purpose.
- **A `reviews` collection for the aggregate:** the right store for diner reviews. It loses for this API because the read is "what is this restaurant's rating right now", and sort by rating has to hit one index with city. The join costs the hot query. We give up per-diner review history until that use case exists.
- **A flags or coordinates collection:** same loss. Flags are three booleans. Coordinates are the geo index key. Splitting either forces a join onto nearest and onto the card.
- **A cuisines collection:** the filter is "this restaurant has this cuisine". A multikey index on the array answers that. A second collection is a join, and a second write whenever the list of cuisines changes. Split them only if a cuisine becomes its own document with its own edits.
- **A collation index instead of `nameLower`:** collation can sort case-insensitively without a copied string. It loses if the index collation and the query collation ever differ, which fails the sort silently. `nameLower` is a normal ascending field that name sort and the rating tie-break already share. The cost is one extra string per restaurant.
- **Store `countryCode` only:** display needs the name, residency needs the code (ADR-007). `CountryNames` can map the code in process. The name is still on the document so a CDC consumer does not need that Java map to render a row. The cost is keeping the two fields in step, which ingest and `RestaurantMapper` do in one place.
- **No counter, client must send an id:** CSV ingest already has source ids. A create call that omits `id` still has to land in the same `Long` `_id` space, so the counter allocates one. The counter has to stay clear of ids the source data already uses.

## Consequences

CDC upserts are `replaceOne({_id})`. GET `/v1/restaurants/{id}` matches the source system.

## Follow-up

Atlas Search is not a second collection. This process runs on Docker Mongo 7, which has no Atlas Search. ADR-004 is the stand-in.

List accepts `maxCostForTwo` and returns `avgCostForTwo` on each row. `POST` accepts the same rating snapshot ingest writes (`averageRating`, `votes`, `ratingText`) so a demo create can be filtered and sorted with the city + rating index. A `reviews` collection is still only for when a diner writes a review.
