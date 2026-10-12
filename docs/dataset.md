# Dataset

Source named in the brief: [Zomato Restaurants Data (Kaggle)](https://www.kaggle.com/datasets/himanshupatil/zomato-restaurants-data).

The repo ships `src/main/resources/data/zomato.csv` — a **small cleaned sample**, Gurgaon-heavy, so Compose/demo/tests stay predictable. Swap in the full Kaggle file under the same path if you want the 9k-row ingest. Encoding is read as ISO-8859-1 because the Kaggle dump is often Latin-1.

## Mapping

| CSV | Document |
|---|---|
| Restaurant ID | `_id` (Long) |
| Restaurant Name | `name` / `nameLower` |
| City, Address, Locality | same |
| Country Code | `countryCode` + `country` via `CountryNames` |
| Longitude, Latitude | GeoJSON `location` `[lng, lat]` |
| Cuisines | `string[]` split on comma |
| Average Cost for two, Currency, Price range | `avgCostForTwo`, `currency`, `priceRange` |
| Has Table booking / Online delivery / delivering now | `flags` |
| Aggregate rating, Votes, Rating text | `rating` |

Rows with blank id or name are skipped. `(0,0)` and out-of-range coords are dropped (kept as "no point") so `$geoNear` does not get junk.

Ingest is idempotent: if `restaurants` already has documents, startup does nothing. After a load I bump `counters.restaurantId` to `max(_id)` so POST without an id does not collide.
