# ADR-007: Security and compliance (MVP vs later)

**Status:** accepted

## Context

DPDP (India) and later GDPR. This collection is a restaurant catalogue, not diners.

## Decision

MVP: validate input, don't publish Mongo's port in Compose, no real auth product. `countryCode` on every document as the future residency key.

Later: OIDC for partner portal, mTLS in cluster, Atlas encryption at rest, India cluster for `countryCode=1`. Right-to-erasure for a restaurant partner is a document delete + CDC; diner PII does not belong here.

## Alternatives

- **API keys on every route now:** theatre for a take-home, and I'd have to put a secret in the README.
- **Putting user location history in this collection:** mixes PII into a public catalogue. Hard no.

## Consequences

The live demo is open on `localhost:8080`. I would not ship that to the internet.

## Follow-up

Residency is a routing problem (`countryCode` → cluster), not a `if (india)` in the resource class.
