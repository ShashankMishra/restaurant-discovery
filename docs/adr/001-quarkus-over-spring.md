# ADR-001: Quarkus (Gradle) over Spring Boot

**Status:** accepted

## Context

Java 21 was the language constraint. Spring Boot would have been the safer interview muscle-memory choice. BiteExpress's 2–3 year goal is cloud-native, not another on-prem WAR.

## Decision

Quarkus 3.40, Gradle wrapper, JVM mode. REST on virtual threads, not Mutiny. Mongo via Panache + raw aggregations.

## Alternatives

- **Spring Boot 3:** faster for me to type, heavier RSS, native image is a second project.
- **Maven:** requested Gradle. Same BOM either way.
- **Micronaut:** fine, smaller community for Mongo examples.

## Consequences

Live edits stay `./gradlew quarkusDev`. Native image is a talking point, not the demo (I will not debug Graal during a follow-up).

## Follow-up

Why not reactive Mongo? Because `$geoNear` plus a live "add this filter" is easier to read as blocking code on virtual threads.
