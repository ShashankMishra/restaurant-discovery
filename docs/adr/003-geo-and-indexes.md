# ADR-003: GeoJSON and indexes

**Status:** accepted

## Context

The list endpoint must sort by distance. Application-side haversine over 9k rows is fine until it isn't, and it is a bad Mongo story.

## Decision

Store `location: { type: "Point", coordinates: [lng, lat] }`. `$geoNear` first in the pipeline. Drop `(0,0)`.

Indexes at startup:

- `location` 2dsphere
- `city` + `rating.aggregate`
- `cuisines` + `city`
- `nameLower`
- text on `name`, `cuisines`, `locality`

## Alternatives

- **`$near` on find:** cannot mix as freely with other filters in one pipeline.
- **Numeric lat/lon + haversine in Java:** easy to write, cannot use the index, dishonest for peak lunch.

## Consequences

Docs without a point never show up in distance sort. Text search cannot share a pipeline with `$geoNear`; leftover NL tokens become a regex when coords are present.

## Follow-up

`$geoNear` needs the 2dsphere index to exist. That's why `MongoIndexCreator` is `@Priority(1)` before ingest.
