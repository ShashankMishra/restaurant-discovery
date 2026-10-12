# API

Base: `/v1`. JSON in and out. Field is `id`, never `_id`.

| Method | Path | What |
|---|---|---|
| POST | `/v1/restaurants` | Create. 201. If `id` omitted, uses a `counters` collection. |
| GET | `/v1/restaurants/{id}` | Full record. 404 if missing. |
| GET | `/v1/restaurants` | Filter + sort + page. |
| GET | `/v1/restaurants/search` | Same as list, but `q` is parsed first. |

## List query params

`name`, `city`, `cuisines` (comma-separated), `minRating`, `maxCostForTwo`, `lat`, `lng`, `maxDistanceKm`, `sort` (`rating` \| `distance` \| `name`), `page` (default 1), `size` (default 20, max 100).

Distance sort needs `lat` and `lng`. `$geoNear` is the first aggregation stage.

List item: `id`, `name`, `address`, `city`, `country`, `cuisines`, `averageRating`, `avgCostForTwo`, `currency`, `distanceKm` (only when geo).

Create does not accept a rating. `averageRating` on a new place stays empty until ingest or a later review flow sets it. A client that sends `averageRating` is ignored.

## Search

`q` required. Optional `lat`/`lng`/`page`/`size`. Parser output is a `RestaurantQuery` and then the same aggregation as list.

## Errors

```json
{ "code": "NOT_FOUND", "message": "restaurant not found: 9", "details": [] }
```

400 `VALIDATION_ERROR` / `BAD_REQUEST`, 404 `NOT_FOUND`, 409 `CONFLICT`.
