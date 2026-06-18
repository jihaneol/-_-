# Obsidian Archive Policy

프로젝트 내부 `work/` 폴더는 현재 개발 기준과 최종본만 유지한다.

수정 이유, 진행 과정, 완료된 작업 기록, 삭제된 작업의 맥락은 Obsidian Idea Lab에 보관한다.

## Obsidian Locations

기본 위치:

```text
/Users/bigs/Documents/Obsidian Vault/02. Area/03. Idea Lab
```

프로젝트별 사용 위치:

```text
07.Build Logs/카드 서비스 작업기록.md
09.Context Handoffs/01.Active Work/card-service.md
04.Decisions/카드 서비스 결정 기록.md
```

## Local Project Files

프로젝트 안에는 현재 상태만 남긴다.

| File | Role | History policy |
|---|---|---|
| `work/00-inbox.md` | 아직 정리되지 않은 새 아이디어 | 처리 후 Obsidian에 기록하고 항목 삭제 가능 |
| `work/01-feature-candidates.md` | 현재 검토 중인 기능 후보 | 승인/거절 후 Obsidian에 기록하고 정리 |
| `work/02-prioritized-roadmap.md` | 현재 우선순위 최종본 | 변경 이유는 Obsidian에 기록 |
| `work/03-active-work.md` | 현재 개발 기준 | 작업 완료 후 Obsidian에 보관하고 다음 작업으로 교체 |
| `work/04-decision-log.md` | 현재 유효한 핵심 결정 | 상세 변경사는 Obsidian에 기록 |
| `work/05-dev-checklist.md` | 현재 체크리스트 최종본 | 변경 이유는 Obsidian에 기록 |
| `work/06-change-log.md` | 최근 완료 요약 | 장기 기록은 Obsidian에 보관 |
| `work/07-git-workflow.md` | 현재 Git 규칙 최종본 | 변경 이유는 Obsidian에 기록 |

## Update Rule

문서를 수정할 때마다 다음을 Obsidian 작업기록에 남긴다.

```text
Date:
Changed:
Why:
Local files:
Decision:
Next:
```

## Completion Rule

작업이 끝나면:

1. Obsidian `07.Build Logs/카드 서비스 작업기록.md`에 완료 내용을 정리한다.
2. Obsidian `09.Context Handoffs/01.Active Work/card-service.md`에 최종 상태와 다음 작업을 남긴다.
3. 프로젝트 `work/03-active-work.md`는 다음 작업 기준으로 교체한다.
4. 완료된 임시 작업 파일이 있다면 삭제한다.
5. 삭제 전 필요한 내용은 Obsidian에 이미 있어야 한다.

## Deletion Rule

삭제해도 되는 파일:

- 완료된 임시 feature brief.
- 완료된 임시 active-work 복사본.
- 더 이상 현재 기준이 아닌 중간 초안.

삭제하면 안 되는 파일:

- `work/00-inbox.md`
- `work/01-feature-candidates.md`
- `work/02-prioritized-roadmap.md`
- `work/03-active-work.md`
- `work/04-decision-log.md`
- `work/05-dev-checklist.md`
- `work/06-change-log.md`
- `work/07-git-workflow.md`
- `work/08-obsidian-archive-policy.md`
- `work/templates/*`

핵심 운영 파일은 삭제하지 않고 현재 최종본으로 갱신한다.
