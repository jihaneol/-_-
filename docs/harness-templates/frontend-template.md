# Frontend Harness Template

Replace placeholders before copying into `docs/frontend-harness`.

## Placeholders

| Placeholder | Meaning |
|---|---|
| `{domain}` | Business domain |
| `{entity}` | Main displayed entity |
| `{core_action}` | Main operator action |
| `{corrective_action}` | Correction/reversal action |
| `{batch_report}` | Batch/reporting workflow |
| `{consistency_check}` | Mismatch/verification workflow |

## Required Frontend Harness Files

- `00-frontend-goal.md`: frontend role and operating workflows.
- `01-frontend-architecture.md`: Feature-Sliced Design Lite structure.
- `02-screen-map.md`: pages, widgets, and user flows.
- `03-api-state-contract.md`: API client, query keys, mutations, invalidation.
- `04-ui-test-strategy.md`: Vitest, React Testing Library, MSW, optional E2E.
- `05-frontend-week-plan.md`: frontend tasks aligned to backend milestones.

## Minimum Evidence

- Dashboard.
- `{entity}` list/detail.
- `{core_action}` form/action.
- `{corrective_action}` confirmation/action.
- `{batch_report}` screen.
- `{consistency_check}` mismatch screen.
- Loading, empty, error, retry, and success states.
- MSW-backed component or flow test.
