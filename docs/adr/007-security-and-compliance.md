# ADR-007: Security and compliance (MVP vs later)

**Status:** accepted

## Context

DPDP applies in India, and GDPR later if a document is about a person. This collection is restaurants. The process is for local use. A residency split has to be possible later without a new document shape.

## Decision

No auth product on this API. Do not publish Mongo's port. Store `countryCode` on every restaurant now.

Three different choices, each with its own reason:

1. **No API keys.** The body of this API is a public catalogue. A shared key in a header would not identify a partner, and it would be a secret to distribute for data that is not private. Partner login is OIDC on the portal, which is the service that has users. This API is not that service. We give up access control here. That holds only while the port stays on the machine running it. It does not hold if `8080` is put on the internet.
2. **No diner data on the restaurant.** A search latitude is a query parameter. Writing it onto the document would make this collection a store of where someone looked, which is personal data with its own retention and erasure. The catalogue has none of that, so a partner erasure is a document delete plus CDC, not a hunt through diner rows.
3. **`countryCode` now, not later.** The code is how a future India cluster is chosen for `countryCode=1`. Adding the field after CDC has already copied the collection is a backfill. Storing it on the write avoids that. Routing stays a cluster choice. It is not a branch in the resource.

Mongo's port stays off the host so the database is not reachable beside the API. Input is validated because a public catalogue is still not an open write of arbitrary fields.

Later, and not in this process: OIDC on the portal, mTLS inside the cluster, encryption at rest, an India cluster keyed by `countryCode`.

## Alternatives

- **API keys on every route now:** a gate on a public read. It loses because the key is shared, so it is not identity, and the data is not confidential. It would matter only as a crude block on a public port, which is the wrong fix for "do not expose `8080`".
- **Store the caller's coordinates on the restaurant:** useful for "searched near here". It loses because those coordinates are diner data. This collection is not the diner store.

## Consequences

`localhost:8080` is open on the machine that runs the process. That is acceptable locally. The same process on the public internet, without the later list above, is not.

## Follow-up

Residency is `countryCode` to a cluster. It is not an `if` in the resource.
