# BiteExpress restaurant discovery

Restaurant search API on MongoDB, plus a thin page at `/` that calls it. How this sits next to BiteExpress's on-prem monolith is in the docs.

## Local run (dev)

Need **JDK 21** and **Docker** (Quarkus Dev Services starts `mongo:7`).

1. If `java -version` is 8, point `JAVA_HOME` at 21 (Gradle also has a 21 toolchain):

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
```

2. Start the app:

```bash
./gradlew quarkusDev
```

Wait for `Listening on: http://localhost:8080`, then:

| What | URL |
|---|---|
| UI | http://localhost:8080/ |
| Swagger | http://localhost:8080/q/swagger-ui |
| Ready | http://localhost:8080/q/health/ready |

First boot loads `src/main/resources/data/zomato.csv` if the collection is empty.

Stop with Ctrl+C. More detail: [docs/setup.md](docs/setup.md). Example calls: [docs/demo.md](docs/demo.md).

## Local run (Compose)

```bash
docker compose up --build
```

Same URLs on port 8080. Mongo is not published on the host. Wipe data with `docker compose down -v`.

## Tests

```bash
./gradlew test
```

HTML report: `build/reports/tests/test/index.html`  
JUnit XML: `build/test-results/test/`

See [docs/testing.md](docs/testing.md).

## Docs

[docs/README.md](docs/README.md) — setup, API, architecture, and ADRs.
