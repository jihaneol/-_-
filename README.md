# card-service

Kotlin Spring Boot backend and React admin frontend for a commerce/payment portfolio project.

## Run Locally

Start MySQL:

```bash
docker compose up -d mysql
```

Start backend:

```bash
./gradlew :bootstrap:bootRun
```

Start frontend:

```bash
npm --prefix frontend run dev -- --host 127.0.0.1 --port 5173
```

Open:

```text
http://127.0.0.1:5173
```

## Validate

Backend:

```bash
./gradlew test
```

Frontend:

```bash
npm --prefix frontend test -- --run
npm --prefix frontend run build
```

Harness validation:

```bash
scripts/backend validate
scripts/frontend validate
```

## Parallel Codex Workflow

Backend and frontend are intentionally split into separate workflow lanes:

```text
workflow/backend/phases/
workflow/frontend/phases/
```

Use separate Codex threads or worktrees for true parallel work:

```bash
scripts/backend status
scripts/frontend status
```

The wrappers are shorthand for:

```bash
python3 scripts/execute.py --lane backend ...
python3 scripts/execute.py --lane frontend ...
```

Shared API expectations live in:

```text
docs/how/05-api-state-contract.md
```

Backend work should not edit `frontend/**`. Frontend work should not edit `modules/**`.
