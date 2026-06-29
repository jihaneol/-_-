# Database Schema Rule

DB schema는 결제 정합성, 중복 방지, 정산/대사 조회를 증명하는 최소 구조로 설계한다.

## 기본 규칙

- schema 변경은 `sql/` 폴더 아래 SQL 파일로 남긴다.
- DDL은 `sql/schema/` 아래에서 테이블별 또는 의미 있는 도메인 묶음별 파일로 분리한다.
- 테이블 정의와 해당 테이블의 index/constraint는 같은 DDL 파일에 둔다.
- 파일 적용 순서가 필요하면 Spring SQL init 설정처럼 실행 설정에서 명시한다.
- entity와 SQL schema는 함께 수정한다.
- `ddl-auto`는 schema 생성 수단으로 사용하지 않는다.
- 금액은 부동소수점이 아니라 정수 minor unit 또는 명확한 decimal 타입으로 저장한다.
- status, type, currency 같은 값은 길이 제한을 둔다.
- 정렬과 추적이 필요한 table은 생성/수정/처리 시각 컬럼을 둔다.

## Constraint Rule

- payment idempotency key에는 unique constraint를 둔다.
- ledger는 append-only 기록으로 보고 update/delete 흐름을 만들지 않는다.
- 정산 summary는 merchant/date 기준 중복 생성을 막는 unique constraint를 둔다.
- outbox event는 재시도와 중복 발행을 추적할 수 있는 식별자와 status를 둔다.
- FK는 조회/정합성 이점과 schema 변경 부담을 비교해 선택하고, 선택 이유를 기록한다.

## Index Rule

- 조회 API, 정산 batch, 대사 report가 사용하는 조건에는 index를 검토한다.
- `merchant_id + id`, `status + id`, `settlement_date + merchant_id`처럼 실제 쿼리 조건 기준으로 잡는다.
- index는 쓰기 비용을 늘리므로 모든 컬럼에 미리 만들지 않는다.
- QueryDSL adapter나 batch query가 생길 때 실행 계획 또는 테스트 데이터 기준으로 보강한다.

## Trade-off

- unique constraint는 중복 방어가 강하지만, application에서 충돌 예외를 정상 흐름으로 다뤄야 한다.
- FK는 데이터 정합성을 DB가 보장하지만, SQL 적용 순서와 대량 적재가 까다로워질 수 있다.
- index는 조회와 batch를 빠르게 하지만, 쓰기 성능과 저장 공간 비용이 든다.
- append-only ledger는 추적성이 좋지만, 정정은 반대 기록이나 보정 기록으로 설계해야 한다.

## Test Rule

- 중요한 unique constraint는 integration test로 검증한다.
- SQL schema와 entity mapping은 integration test 또는 별도 schema 검증으로 확인한다.
- batch/report query index는 데이터가 늘어나는 시점에 실행 계획을 확인한다.

## Reference

- JPA/entity 규칙: `rules/jpa-entity-rule.md`
- 동시성 규칙: `rules/concurrency-rule.md`
- 트랜잭션 규칙: `rules/transaction-rule.md`
