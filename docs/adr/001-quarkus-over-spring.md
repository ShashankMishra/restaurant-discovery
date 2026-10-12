# ADR-001: Quarkus (Gradle) over Spring Boot

**Status:** accepted

## Context

This is one discovery API beside an existing monolith. Dev and tests need Mongo without a database we start by hand. The process should stay small, not become a second full application stack.

## Decision

Quarkus 3.40 instead of Spring Boot, for two reasons:

1. **Dev Services.** `quarkusDev` and the tests start `mongo:7` with the app. The search pipeline can be edited and retested without a separate database lifecycle.
2. **A smaller process.** Discovery sits next to the monolith. Quarkus starts faster and uses less memory than another full Spring Boot app for a single read API.

Gradle, JVM mode, and virtual threads are choices inside that runtime. They are not why Spring Boot lost.

Mongo via Panache for persist/find, and a raw aggregation for list and geo, because `$geoNear` has to stay visible. Spring Boot can show that pipeline too. Visibility did not decide the framework.

## Alternatives

- **Spring Boot 3:** can do Java 21, a visible `$geoNear` pipeline, virtual threads, live reload, and a native image. Those are not the difference. It does not start Mongo with the test and dev process the way Dev Services does here, and it is a larger baseline for one resource. We give up the more familiar stack.
- **Maven:** the Quarkus BOM is the same either way. Nothing here is multi-module. The repo is already a Gradle wrapper, so Maven would be a second build for no gain.
- **Micronaut:** a reasonable runtime, with a thinner Dev Services path for this Mongo pipeline. Quarkus is the one whose dev mode starts `mongo:7` for both `quarkusDev` and tests.
- **Mutiny / reactive Mongo:** this is inside Quarkus, not against Spring. The driver calls are blocking. Virtual threads keep `RestaurantQueryService` as straight-line code. Reactive would split the same aggregation across callbacks, with no throughput win at this catalogue size.
- **Native image now:** also inside the Quarkus choice. The Mongo client and reflection are a separate build. JVM is what Compose and `quarkusDev` run. Native stays later, and Spring Boot has a native image too, so this was never the reason to switch.

## Consequences

Live edits stay `./gradlew quarkusDev`, with Mongo coming up beside them. A native image is out of scope for this service.

## Follow-up

A native image is worth it only after the query shape stops changing. Until then the cost is Graal config, not latency we have measured.
