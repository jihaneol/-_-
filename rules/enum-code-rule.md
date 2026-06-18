# Enum Code Rule

enum은 코드에서 분기와 저장을 안정적으로 하기 위한 영어 식별자와, 사람이 이해할 수 있는 한글 표시명을 함께 가진다.

## 기본 규칙

```kotlin
enum class PaymentStatus(
    val label: String,
) {
    AUTHORIZED("승인 완료"),
    CANCELLED("취소 완료"),
    SETTLED("정산 완료"),
    FAILED("실패"),
}
```

규칙:

- enum value 이름은 `AUTHORIZED`처럼 영어 대문자 식별자로 둔다.
- 사용자가 보거나 API 문서에서 설명해야 하는 enum은 `label` 같은 한글 표시 필드를 반드시 둔다.
- API 응답에 enum이 노출되면 안정적인 코드 값과 한글 표시명을 함께 내려준다.
- 에러 메시지와 사용자 표시 문구는 한글로 작성한다.

## API Response 예시

```kotlin
data class PaymentResponse(
    val paymentStatus: String,
    val paymentStatusLabel: String,
)
```

```json
{
  "paymentStatus": "AUTHORIZED",
  "paymentStatusLabel": "승인 완료"
}
```
