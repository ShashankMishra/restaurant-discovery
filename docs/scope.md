# Scope

The brief says 3–5 hours and that finishing every line is not the point. I cut it this way.

## In the running MVP

- Mongo as the store for restaurants
- POST / GET-by-id / list (name, city, cuisine, rating, distance) / NL search
- Indexes: 2dsphere, city+rating, cuisines+city, text, nameLower
- Compose + `quarkusDev`
- One static discovery page (`/`) wired to the APIs — not a product UI
- Unit, integration, ArchUnit
- Docs for coexistence, peak traffic, residency

## Written, not built

- Dual-write / Debezium CDC from the Gurugram RDBMS
- Atlas Search (this NL parser is a stand-in)
- Cursor pagination, Redis, read replicas
- GraalVM native image
- OIDC / API keys as a product
- Multi-region by `countryCode`

## Out

- Any consumer UI
- Orders, payments, riders
- Pretending this API is already the system of record for the whole platform
