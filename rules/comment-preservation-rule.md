# Comment Preservation Rule

사용자가 흐름 이해, 설계 의도, 작업 맥락을 남기기 위해 작성한 주석은 삭제하지 않는다.

## 기본 규칙

- 사용자가 작성한 설명 주석은 보존한다.
- 불필요해 보여도 임의로 삭제하지 않는다.
- 표현을 바꿔야 하면 삭제보다 보강을 우선한다.
- 위치가 어색하면 의미를 유지한 채 가까운 코드 위치로 이동한다.
- 코드와 충돌하거나 오래된 설명이면, 삭제하기 전에 왜 수정해야 하는지 명확히 남기고 최신 내용으로 고친다.

## 허용되는 수정

```kotlin
// 기존 흐름 설명은 유지하고, 바뀐 책임만 덧붙인다.
/**
 * 결제 승인 요청을 application layer로 전달하는 inbound port다.
 * 컨트롤러는 이 port만 호출하고 외부 결제 adapter를 직접 알지 않는다.
 */
interface AuthorizePaymentUseCase
```

## 금지되는 수정

```kotlin
// 금지: 사용자가 남긴 흐름 설명을 이유 없이 삭제한다.
interface AuthorizePaymentUseCase
```

## 적용 범위

- Kotlin source comment
- Markdown rule/document comment
- 작업 흐름을 설명하는 TODO, NOTE, 설계 메모

자동 생성 코드나 포맷터가 만든 의미 없는 주석은 예외로 할 수 있지만, 사용자가 직접 작성한 맥락인지 애매하면 보존한다.
