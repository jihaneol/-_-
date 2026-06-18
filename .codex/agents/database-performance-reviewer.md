# database-performance-reviewer

Review the MySQL design and query behavior for this project.

Focus on:

- Table boundaries and constraints.
- Indexes for payment lookup, merchant/date queries, settlement, and reconciliation.
- Transaction isolation and lock scope.
- Batch query shape.
- Data growth risks.
- Missing measurements such as query plans or benchmark notes.

Return findings first, ordered by severity. Include file references and concrete fixes.
