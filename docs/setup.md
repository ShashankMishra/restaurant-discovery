# Setup

## Need

- JDK 21 (Gradle toolchain is pinned to 21 even if `JAVA_HOME` is 8)
- Docker (for Compose demo, or for tests/dev if Dev Services can pull `mongo:7`)
- That's it. Gradle is the wrapper in the repo.

## Dev (fastest for a live tweak)

Docker must be running. Then:

```bash
# optional if the default `java` is still 8
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"

./gradlew quarkusDev
```

Quarkus starts a Mongo 7 container by itself. Ingest runs once if the collection is empty.

| | |
|---|---|
| UI | http://localhost:8080/ |
| API | http://localhost:8080/v1/restaurants |
| Swagger | http://localhost:8080/q/swagger-ui |
| Ready | http://localhost:8080/q/health/ready |

Ctrl+C stops the process. Dev Services Mongo goes away with it unless you keep the container.

## Docker Compose

```bash
docker compose up --build
```

- API: `8080`
- Mongo is not published on the host on purpose
- Connection string inside the compose network: `mongodb://mongo:27017`
- Database: `biteexpress`

Reset data:

```bash
docker compose down -v
```

## Useful config

| Key | Default |
|---|---|
| `quarkus.mongodb.connection-string` | Dev Services in `%dev`/`%test`; `mongodb://mongo:27017` in `%prod` |
| `quarkus.mongodb.database` | `biteexpress` |
| `discovery.ingest.enabled` | `true` (`false` in tests) |
| `discovery.ingest.resource` | `data/zomato.csv` |

To load a full Kaggle dump, drop the file on the classpath as `data/zomato.csv` (see [dataset.md](dataset.md)). The bundled file is a small Gurgaon-heavy sample so the demo is deterministic.
