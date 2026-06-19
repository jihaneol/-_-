# Rule Authoring Rule

`rules/` 문서는 구현자가 매번 읽고 적용할 수 있을 만큼 작고 명확해야 한다.

## 기본 규칙

- 하나의 rule file은 하나의 관심사만 다룬다.
- 새 규칙은 기본값, 금지할 것, 테스트 기준, trade-off만 남긴다.
- 구현 예시는 꼭 필요할 때만 짧게 넣고, 긴 예시는 `docs/` reference로 분리한다.
- 특정 기술을 영구 고정하지 말고, 현재 기본 선택과 바꿀 수 있는 조건을 함께 적는다.
- 이미 다른 rule에 있는 내용을 복사하지 말고 reference로 연결한다.
- 규칙이 충돌하면 `AGENT.md`의 authority order를 따른다.

## Size Rule

- 새 rule file은 기본적으로 40-80줄 안에서 끝낸다.
- 한 section은 3-7개 bullet을 넘기지 않는다.
- 체크리스트처럼 계속 늘어나는 문서로 만들지 않는다.
- 예외가 많아지면 하위 section을 늘리기보다 별도 rule file로 분리한다.

## Trade-off And Problem Rule

- 중요한 선택에는 trade-off를 함께 적는다.
- trade-off는 장점만 쓰지 말고 비용, 복잡도, 운영 부담, 테스트 부담을 같이 적는다.
- 문제점은 공포 목록이 아니라 실제 구현자가 놓치기 쉬운 위험만 적는다.
- 해결책이 아직 정해지지 않은 문제는 강제 규칙으로 쓰지 않고 `검토한다`, `선택한다`, `기록한다`로 표현한다.

## Extensibility Rule

- 현재 MVP에서는 단순한 기본값을 우선한다.
- 확장이 필요한 지점은 interface, port, adapter, outbox, batch 단위처럼 바꿀 수 있는 경계로 남긴다.
- Kafka/RabbitMQ, Redis, cloud 같은 선택지는 core flow가 안정된 뒤 도입한다.
- 확장 가능성을 이유로 지금 필요 없는 framework, 추상화, 설정을 먼저 만들지 않는다.

## Must Not

- 모든 상황을 미리 커버하려고 긴 정책 문서를 만들지 않는다.
- 구현 세부 코드를 rule 문서에 길게 붙이지 않는다.
- "항상", "절대"는 데이터 정합성, 보안, 사용자 요청 보존처럼 실제로 강한 기준일 때만 쓴다.
- trade-off 없이 특정 패턴만 정답처럼 고정하지 않는다.

## Reference

- 운영 기준: `AGENT.md`
- 기록 기준: `rules/obsidian-archive-policy.md`
- 아키텍처 규칙: `rules/backend-architecture.md`
