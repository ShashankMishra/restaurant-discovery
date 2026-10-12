# ADR-005: Coexistence with the monolith

**Status:** accepted (docs only in the MVP)

## Context

BiteExpress cannot freeze partner-portal writes while we play with Mongo.

## Decision

For production: RDBMS stays SoR; Debezium (or equivalent) upserts into Mongo on Restaurant ID. This API is a read model for discovery. The POC treats Mongo as SoR so the demo can POST.

If CDC lag is a problem, dual-write with an outbox. Never "write Mongo then RDBMS" as the first try — the monolith still owns payments-adjacent restaurant flags.

## Alternatives

- **Big-bang cutover:** high blast radius, no peak-hour safety net.
- **Mongo as SoR immediately:** then the partner portal and every report have to move together. That's not a 6–12 month plan.

## Consequences

`_id` choice in ADR-002 is for this. Delete/update in RDBMS must stream or you serve ghosts.

## Follow-up

Failure mode they will ask: RDBMS commit succeeded, Mongo apply failed. Answer: retry from the CDC offset, not from the API thread; discovery may be stale for seconds, orders still work on the monolith.
