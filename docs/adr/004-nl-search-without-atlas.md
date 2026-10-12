# ADR-004: NL search is a parser, not an LLM

**Status:** accepted

## Context

`GET /v1/restaurants/search` has to handle the three example strings. The live demo cannot depend on an OpenAI key or Atlas Search.

## Decision

Deterministic parser → the same `RestaurantQuery` as the list endpoint. Seed vocabulary so "Gurgaon" / "South Indian" / "Vada Pav" work even on an empty test DB.

## Alternatives

- **LLM → MQL:** demo-flaky, and you need a deny-list (`$where`, `$lookup` to other DBs, …).
- **Atlas Search compound operators + synonyms:** the actual product path. Needs Atlas. Written here, not running.

## Consequences

The parser will misread odd phrasing. That is acceptable if I can show how Atlas Search replaces it: should/must clauses on city, cuisine, range on `avgCostForTwo`, geo filter, ranking.

## Follow-up

I would not put an LLM on the hot path of a food app at lunch. If we add one, it produces structured JSON that still goes through `RestaurantQuery`, never raw MQL.
