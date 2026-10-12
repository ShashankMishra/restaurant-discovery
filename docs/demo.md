# Demo (about 10 minutes)

Assume Compose or `quarkusDev` is up.

1. Ready check

```bash
curl -s http://localhost:8080/q/health/ready
```

2. Open http://localhost:8080/ — Search hits `/v1/restaurants/search`, Browse hits `/v1/restaurants`, Add posts a restaurant, Look up loads `/v1/restaurants/{id}`. For “nearest” or distance sort, set the starting point to Gurgaon center (28.4595, 77.0266).

3. Swagger: http://localhost:8080/q/swagger-ui

4. Create

```bash
curl -s -X POST http://localhost:8080/v1/restaurants \
  -H 'Content-Type: application/json' \
  -d '{"name":"Live Edit Kitchen","city":"Gurgaon","cuisines":["Cafe"],"averageRating":4.0,"latitude":28.46,"longitude":77.03}'
```

5. Get by id (use the `id` from step 4)

```bash
curl -s http://localhost:8080/v1/restaurants/1
```

Bundled sample ids work after ingest, e.g. `18241590` (Sagar Ratna).

6. List / filter

```bash
curl -s 'http://localhost:8080/v1/restaurants?city=Gurgaon&cuisines=South%20Indian&sort=rating'
```

7. Distance (Cyber Hub-ish point)

```bash
curl -s 'http://localhost:8080/v1/restaurants?lat=28.495&lng=77.088&sort=distance&maxDistanceKm=10'
```

8. Natural-language search

```bash
curl -s --get http://localhost:8080/v1/restaurants/search \
  --data-urlencode 'q=Best restaurants in Gurgaon with South Indian Food'

curl -s --get http://localhost:8080/v1/restaurants/search \
  --data-urlencode 'q=Restaurants in Gurgaon under Rs. 1000 for two'

curl -s --get 'http://localhost:8080/v1/restaurants/search' \
  --data-urlencode 'q=Nearest place that serves Vada Pav' \
  --data-urlencode 'lat=28.4595' \
  --data-urlencode 'lng=77.0266'
```

If they ask for a live change, the cheap ones are: a new query param on `GET /v1/restaurants`, a line in `NaturalLanguageParser`, or an index in `MongoIndexCreator`. Stay in `service/` — don't reach Mongo from the resource.
