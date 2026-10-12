# ADR-004: NL search is a parser, not an LLM

**Status:** accepted

## Context

`GET /v1/restaurants/search` has to answer the example sentences ("best … in Gurgaon", "under Rs. 1000 for two", "nearest … Vada Pav") through the same list pipeline as the filter API. Tests have to assert the result of a sentence. The database is MongoDB 7, with no Atlas account.

## Decision

A deterministic parser fills a `RestaurantQuery`. The list endpoint runs that object. Nothing else does.

Two rejections, for different reasons:

1. **Not an LLM.** A model can read more phrasings than this parser. It loses because the same sentence must parse the same way every time, and because the model must not emit Mongo query language. `$where` and a `$lookup` into another database are the failure. If a model is added later, it may only fill fields on `RestaurantQuery`.
2. **Not Atlas Search yet.** Atlas is the better search engine: synonyms, a compound of city, cuisine, price, and geo, then a relevance rank. It loses here because this process talks to `mongodb://mongo:27017`, which cannot run Atlas Search. The parser is the stand-in. Odd sentences will miss. The API does not change when Atlas replaces the parser: search still ends in a `RestaurantQuery`.

A small vocabulary makes "Gurgaon", "South Indian", and "Vada Pav" resolve even when the test database is empty. That is so the parser tests do not depend on ingest.

## Alternatives

- **LLM that emits MQL:** handles phrasing the vocabulary misses. Non-deterministic, a network call on the request, and an injection problem unless the output is denied down to a known shape. We give up that phrasing. The deny-list stays in one place only if the model never writes the pipeline.
- **Atlas Search operators now:** the product end state, and unavailable on this Mongo. We give up synonyms and relevance ranking until the engine exists. We do not give up the resource or the query object.

## Consequences

Odd sentences miss. Search does not grow a second pipeline beside list.

## Follow-up

A model does not belong on the request path. Structured fields into `RestaurantQuery` are the only output that keeps the query under the same validation as a hand-built filter.
