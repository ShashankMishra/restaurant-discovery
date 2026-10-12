# BiteExpress restaurant discovery

Restaurant search API on MongoDB, plus a thin page at `/` that calls it. How this sits next to BiteExpress's on-prem monolith is in the docs.

## Run with Docker

Docker is the only install. It builds the API and starts Mongo.

```bash
docker compose up --build
```

Wait until the API is up, then:

| What | URL |
|---|---|
| UI | http://localhost:8080/ |
| Swagger | http://localhost:8080/q/swagger-ui |
| Ready | http://localhost:8080/q/health/ready |

First boot loads `src/main/resources/data/zomato.csv` if the collection is empty.

Mongo is not published on the host. Wipe data with `docker compose down -v`.

## Run locally

Install **JDK 21** (`java -version` reports 21) and **Docker**. Then:

```bash
./gradlew quarkusDev
```

Same URLs. Quarkus Dev Services starts `mongo:7`. Stop with Ctrl+C.

More detail: [docs/setup.md](docs/setup.md). Example calls: [docs/demo.md](docs/demo.md).

## Tests

Install JDK 21, then:

```bash
./gradlew test
```

HTML report: `build/reports/tests/test/index.html`  
JUnit XML: `build/test-results/test/`

See [docs/testing.md](docs/testing.md).

## Docs

[docs/README.md](docs/README.md) — setup, API, architecture, and ADRs.
