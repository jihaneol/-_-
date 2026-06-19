# Why This Shape

## Selection Reasons

The project is optimized to prove backend hiring signals: financial correctness, concurrency handling, transaction boundaries, test discipline, and explainable architecture.

## Tradeoffs

- Domain and JPA entity are combined to keep the portfolio implementation small while still preserving module boundaries.
- Kafka/RabbitMQ is deferred because ledger correctness and idempotency must be proven before async delivery adds value.
- Frontend work is delayed until API behavior is stable enough for reliable MSW contracts.
- QueryDSL read adapters are part of the target architecture, but command correctness comes first.

## Decision Rule

When a feature changes direction, update `docs/why/` with the reason before changing phase files. Phase files should contain the executable contract, not the full debate history.

## Primary References

- `docs/why/00-capability-map.md`
- `docs/why/01-agent-review-loop.md`
- `docs/why/02-dev-log.md`

