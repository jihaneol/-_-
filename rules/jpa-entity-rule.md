# JPA Entity Rule

이 프로젝트는 도메인 모델과 JPA entity를 같은 클래스로 사용한다.

## Placement

```text
modules/domain/src/main/kotlin/com/example/cardservice/domain/{domain}/model
```

규칙:

- entity class는 `domain` 모듈의 `{domain}.model` 패키지에 둔다.
- 좁은 Spring Data `Repository<T, ID>` 계약은 `application/provided`에 둘 수 있다.
- `JpaRepository`처럼 넓은 Spring Data interface는 사용하지 않는다.
- QueryDSL repository와 persistence adapter는 `infra` 모듈에 둔다.
- entity는 Spring `@Service`, `@Component`, repository, QueryDSL, web DTO를 직접 알면 안 된다.

## ID Rule

PK는 `id`로 통일한다.

```kotlin
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "id")
var id: Long? = null
    protected set
```

규칙:

- auto increment PK 컬럼명은 `id`를 사용한다.
- `payment_sequence` 같은 별도 PK 이름을 만들지 않는다.
- 별도 public id가 필요하다는 요구가 생기기 전에는 `paymentId` 같은 중복 식별자 컬럼을 만들지 않는다.
- domain value object가 필요하면 `PaymentId(id)`처럼 PK를 감싼다.

## Field Access Rule

JPA는 field access를 사용한다.

```kotlin
@Entity
@Access(AccessType.FIELD)
@Table(name = "payments")
class Payment protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null
        protected set
}
```

규칙:

- `@Access(AccessType.FIELD)`를 사용한다.
- JPA 컬럼은 private backing field로 둔다.
- domain value object 접근은 계산 property getter로 제공할 수 있다.
- `@get:Transient`는 사용하지 않는다.

## Domain Computed Property Rule

JPA 컬럼으로 저장되는 primitive field를 value object로 감싸서 돌려줄 때는 계산 property로 제공한다.

```kotlin
@Column(name = "amount", nullable = false)
private var amountValue: Long = 0

@Column(name = "currency", nullable = false, length = 3)
private var currencyValue: String = ""

val money: Money
    get() = Money(amount = amountValue, currency = currencyValue)
```

규칙:

- `@Access(AccessType.FIELD)`를 명시했기 때문에 JPA는 field를 기준으로 매핑한다.
- 계산 property에는 JPA annotation을 붙이지 않는다.
- `@get:Transient`를 붙이지 않는다.
- application layer는 backing field를 알지 않고 계산 property만 사용한다.

## Constructor Rule

```kotlin
class Payment protected constructor() {
    private constructor(
        merchantId: MerchantId,
        money: Money,
    ) : this() {
        this.merchantIdValue = merchantId.value
        this.amountValue = money.amount
        this.currencyValue = money.currency
    }
}
```

규칙:

- JPA용 기본 생성자는 `protected constructor()`로 둔다.
- 생성은 companion object factory나 도메인 메서드로 제공한다.
- 비즈니스 검증은 value object 또는 factory에서 유지한다.

## SQL Schema Rule

entity를 만들거나 컬럼을 바꾸면 `sql/` 아래 schema SQL도 함께 수정한다.

DB schema, constraint, index 세부 기준은 `rules/database-schema-rule.md`를 따른다.

```sql
CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    merchant_id VARCHAR(100) NOT NULL,
    order_id VARCHAR(100) NOT NULL,
    idempotency_key VARCHAR(150) NOT NULL,
    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_payments_idempotency_key UNIQUE (idempotency_key)
);
```

규칙:

- schema SQL과 entity가 맞아야 한다.
- unique constraint는 entity와 schema SQL 양쪽에 맞춘다.
- repository는 schema를 만들지 않는다.

## Repository Contract Rule

Spring Data repository를 사용할 때는 필요한 메서드만 가진 좁은 계약으로 둔다.

```kotlin
interface PaymentRepository : Repository<Payment, Long> {
    fun save(payment: Payment): Payment
}
```

규칙:

- repository 계약은 `application/{domain}/provided`에 둘 수 있다.
- `JpaRepository`를 상속하지 않는다.
- 필요한 메서드만 명시한다.
- adapter는 `infra`에서 repository 계약을 주입받아 application port를 구현한다.
- QueryDSL adapter는 계속 `infra`에 둔다.

## Reference

- DB schema 규칙: `rules/database-schema-rule.md`
- 동시성 규칙: `rules/concurrency-rule.md`
