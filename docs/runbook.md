# Runbook

| Check | URL |
|---|---|
| Health | `/q/health` |
| Ready (includes Mongo) | `/q/health/ready` |
| Swagger | `/q/swagger-ui` |

Logs are the Quarkus console in dev. In Compose: `docker compose logs -f api`.

## Things that actually happen

**Ready never goes up.** Mongo isn't healthy yet, or `QUARKUS_MONGODB_CONNECTION_STRING` is wrong. In Compose it must be `mongodb://mongo:27017`.

**Empty list after Compose up.** Ingest only runs on an empty collection. `docker compose down -v` and up again. Or POST a restaurant.

**`$geoNear` errors about a 2dsphere index.** `MongoIndexCreator` should have run at startup. If you wiped indexes by hand, restart the API.

**`sort=distance` → 400.** You didn't pass `lat` and `lng`. Same for NL "nearest" without coords.

**Live follow-up edit.** `./gradlew quarkusDev` and change Java; don't rebuild the image unless they want to see Compose.

JVM container is the demo. Native image is a later conversation, not something I would flip during the interview.
