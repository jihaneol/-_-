# Port Adapter Comment Rule

port와 adapter는 파일 상단에 짧은 KDoc으로 역할을 설명한다.

## Required Port

컨트롤러, 배치, 메시지 수신 같은 inbound adapter가 application use case를 호출하는 진입점이다.

```kotlin
/**
 * 결제 승인 요청을 application layer로 전달하는 inbound port다.
 */
interface AuthorizePaymentUseCase {
    fun authorize(input: AuthorizePaymentInput): AuthorizePaymentResult
}
```

## Provided Port

application service가 DB, 외부 API, 메시지 발행 같은 바깥 기능을 요청하는 outbound port다.

```kotlin
/**
 * 승인된 결제 aggregate를 저장하기 위해 application service가 호출하는 outbound port다.
 */
interface SavePaymentPort {
    fun save(payment: Payment): Payment
}
```

Spring Data repository 계약을 application에 둘 때도 필요한 메서드만 노출한다.

```kotlin
/**
 * Payment entity 저장을 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface PaymentRepository : Repository<Payment, Long> {
    fun save(payment: Payment): Payment
}
```

## File Split Rule

- provided port/repository 파일은 하나의 top-level interface만 담는다.
- `CommerceRepositories.kt`처럼 여러 aggregate repository와 query/lock port를 한 파일에 묶지 않는다.
- 같은 aggregate에 강하게 붙은 메서드는 하나의 repository interface 안에 둘 수 있지만, 서로 다른 aggregate나 서로 다른 adapter 책임은 파일을 분리한다.
- 파일명은 interface명과 맞춘다. 예: `MemberRepository.kt`, `CouponQueryPort.kt`, `CommerceLockPort.kt`.

## Adapter

adapter는 특정 기술이나 외부 시스템으로 port를 구현한다.

```kotlin
/**
 * SavePaymentPort를 Spring Data JPA repository로 구현하는 persistence adapter다.
 */
@Component
class JpaPaymentAdapter : SavePaymentPort {
    override fun save(payment: Payment): Payment = TODO()
}
```

규칙:

- port와 adapter 타입 선언 바로 위에 KDoc을 작성한다.
- 설명은 한글로 작성한다.
- 설명에는 연결 방향과 기능을 포함한다.
- 구현 세부사항을 길게 설명하지 않는다.
- service, controller, domain model에는 이 규칙을 강제하지 않는다.
- 사용자가 흐름 이해를 위해 작성한 기존 주석은 `rules/comment-preservation-rule.md`에 따라 삭제하지 않는다.
