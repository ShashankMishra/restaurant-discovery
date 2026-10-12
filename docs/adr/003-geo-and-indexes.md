# ADR-003: GeoJSON and indexes

**Status:** accepted

## Context

Distance sort has to use an index. The same request also filters city, cuisine, rating, and cost, and it still has to be able to sort by rating or name. The response needs kilometres.

## Decision

Store `location` as a GeoJSON point and run `$geoNear` as the first pipeline stage.

`$near` is the option people reach for first. It loses because it always orders by distance. This API must also sort by rating and by name after the distance is known. `$geoNear` uses the 2dsphere index, writes `distanceMeters`, and then the pipeline sorts. The cost is the stage-order rule: `$geoNear` is first, so the other filters go in its `query` rather than a later `$match`.

Haversine in Java loses because it cannot use that index. Every candidate document comes back to the process. Distance is the part Mongo is here for.

`(0,0)` is treated as missing. The CSV uses that pair when a restaurant has no coordinates. Leaving it in would make those rows the nearest hit for anyone near the Gulf of Guinea. `RestaurantMapper.isValidCoord` drops them. Rows with no point never appear in a distance sort.

Indexes created at startup by `MongoIndexCreator`, one per filter the query actually builds:

- `location` 2dsphere, for `$geoNear`
- `city` + `rating.aggregate`, for the city browse sorted by rating
- `cuisines` + `city`, for a cuisine inside a city
- `nameLower`, for name sort
- text on `name`, `cuisines`, `locality`, for search when there is no geo stage

## Alternatives

- **`$near` on a find:** indexed, and it returns distance, but the order is fixed as nearest-first. A rating sort on that same query fights the operator. `$geoNear` is the stage that separates "compute distance" from "sort".
- **Numeric lat/lon and haversine in Java:** no special index, no stage-order rule, and a full read of the candidates. Wrong once one city is the hot key.
- **Keep `(0,0)` as a real point:** simpler ingest. It poisons nearest-search. Dropping it means a restaurant with no coordinates is invisible to geo, which is what we want.

Text cannot share a pipeline with `$geoNear`. When the request has coordinates, leftover words become a regex on name and cuisine instead of the text index. That is the price of distance and words in one request.

## Consequences

Documents without a point never show up in distance sort. A geo request does not use the text index.

## Follow-up

`$geoNear` fails if the 2dsphere index is missing. `MongoIndexCreator` is `@Priority(1)` so the index exists before ingest writes points.
