# How We Build

## Data Flow

Write flows enter through inbound adapters, call application use cases, enforce domain invariants, and persist through required ports. Read flows use separate query ports and read adapters.

```text
HTTP or batch adapter
  -> application use case
  -> domain model
  -> required port
  -> infra or external adapter
```

## Patterns

- DDD aggregate and value object model.
- Hexagonal module boundaries.
- CQRS split between command use cases and read use cases.
- Behavior-style TDD for domain and application behavior.
- Integration tests for persistence and transaction boundaries.

## Primary References

- `docs/how/00-architecture.md`
- `docs/how/01-domain-model.md`
- `docs/how/02-api-contract.md`
- `docs/how/03-test-strategy.md`
- `docs/how/04-frontend-architecture.md`
- `docs/how/05-api-state-contract.md`
- `docs/how/06-ui-test-strategy.md`
- `docs/how/07-architecture-diagram.md`

