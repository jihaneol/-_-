# JPA Entity Rule

이 프로젝트는 도메인 모델과 JPA entity를 같은 클래스로 사용한다.

## Shape

```text
modules/domain/src/main/kotlin/com/example/cardservice/domain/{domain}/{Aggregate}.kt
```

```kotlin
@Entity
@Access(AccessType.FIELD)
@Table(
    name = "payments",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_payments_idempotency_key", columnNames = ["idempotency_key"]),
    ],
)
class Payment protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0L
        protected set

    @Column(name = "amount", nullable = false)
    private var amountValue: Long = 0

    val money: Money
        get() = Money(amount = amountValue, currency = currencyValue)

    private constructor(...): this() { ... }

    companion object {
        fun authorize(...): Payment = Payment(...)
    }
}
```

Current example: `modules/domain/src/main/kotlin/com/example/cardservice/domain/payment/model/Payment.kt`

## Placement

```text
domain/member/Member.kt
domain/product/Product.kt
domain/order/Order.kt
domain/order/OrderItem.kt
domain/coupon/Coupon.kt
domain/coupon/CouponHistory.kt
domain/payment/model/Payment.kt
domain/payment/operation/PaymentOperationRecord.kt
```

- aggregate/entity 단위로 파일을 나눈다.
- enum은 해당 entity 그룹 패키지에 둔다.
- `domain/commerce`, `{domain}/model` 같은 범용 묶음 폴더를 새로 만들지 않는다.
- `CommerceRequests.kt/CommerceResponses.kt`처럼 여러 aggregate를 한 파일에 모으지 않는다.

## Mapping Defaults

```kotlin
@Access(AccessType.FIELD)
@Column(name = "merchant_id", nullable = false)
private var merchantIdValue: Long = 0

val merchantId: MerchantId
    get() = MerchantId(merchantIdValue)
```

- PK: `id: Long = 0L`; `0L`은 transient sentinel
- public id 요구 전까지 `paymentId` 같은 중복 식별자 컬럼 금지
- field access 사용; JPA annotation은 backing field에 둔다.
- 계산 property에는 JPA annotation과 `@get:Transient`를 붙이지 않는다.
- 생성은 companion factory/domain method로 제공한다.

## Repository And Query

```kotlin
interface PaymentRepository : Repository<Payment, Long> {
    fun save(payment: Payment): Payment
}
```

- 좁은 Spring Data `Repository<T, ID>` 계약은 `application/{domain}/provided`에 둘 수 있다.
- `JpaRepository` 상속 금지.
- QueryDSL adapter와 persistence adapter는 `infra`에 둔다.
- entity source는 `com.querydsl.*`, repository, web DTO, Spring service/component를 알면 안 된다.

## Schema

```sql
CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    merchant_id BIGINT NOT NULL,
    idempotency_key VARCHAR(150) NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_payments_idempotency_key UNIQUE (idempotency_key)
);
```

- entity 변경 시 `sql/` schema도 함께 수정한다.
- PK/FK성 ID와 이를 감싸는 value object는 `Long`.
- 외부 승인키, idempotency key, event key처럼 실제 문자열 식별자만 `String`.
- unique constraint는 entity와 schema SQL 양쪽을 맞춘다.

## Avoid

```kotlin
class PaymentProjection @Entity
interface PaymentRepository : JpaRepository<Payment, Long>
@get:Transient val money: Money
private val query = QPayment.payment
data class CommerceRequests(...)
```

## References

- `rules/database-schema-rule.md`
- `rules/concurrency-rule.md`
