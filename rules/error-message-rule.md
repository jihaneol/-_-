# Error Message Rule

사용자 또는 API client에게 노출될 수 있는 에러 메시지는 한글로 작성한다.

## Scope

적용 대상:

- domain `require`, `check`, `requireNotNull` 메시지
- application service 예외 메시지
- controller/global handler 응답 메시지
- API contract의 에러 메시지 예시
- 테스트에서 기대하는 에러 메시지

적용 제외:

- `VALIDATION_ERROR`, `BAD_REQUEST`, `INTERNAL_SERVER_ERROR` 같은 error code
- class, method, package, enum, test framework API 이름
- Gradle wrapper나 외부 도구가 제공하는 메시지

## Rule

```kotlin
// 허용
require(amount > 0) { "금액은 0보다 커야 합니다." }
require(value > 0) { "주문 ID는 0보다 커야 합니다." }

// 금지
require(amount > 0) { "amount must be positive" }
require(value > 0) { "orderId must be positive" }
```

## Response Rule

에러 응답의 `code`는 안정적인 식별자이므로 영어 대문자 snake case를 사용한다.

에러 응답의 `message`는 한글 문장으로 작성한다.

```json
{
  "code": "BAD_REQUEST",
  "message": "쿠폰 수량은 1개 이상이어야 합니다."
}
```
