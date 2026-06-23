# QueryDSL Rule

QueryDSL은 조회 전용 read adapter 구현 기술이다. command service, domain entity, controller에서 QueryDSL을 직접 사용하지 않는다.

Spring Data `Pageable`은 금지가 아니다. 단순 단일 aggregate 조회는 Spring Data repository에서 `Pageable`을 써도 된다. QueryDSL은 조건이 커지거나 projection/read model이 필요한 조회에 적용한다.

## Technology Selection Rule

| Case | Rule |
|---|---|
| 단일 entity 기준 단순 목록/상세 | Spring Data repository + `Pageable` 허용 |
| join, 동적 검색 조건, 여러 필드 projection | QueryDSL 사용 |
| 운영자 대시보드, 정산/대사 리포트, batch 검증 화면 | QueryDSL 사용 |
| count query 최적화가 필요한 대량 목록 | QueryDSL + `PageableExecutionUtils` 사용 |

공통 기준:

- controller는 `page`, `size`, `sort` request parameter만 받고 Spring `Pageable`을 직접 받지 않는다.
- application use case/port는 공통 `Pagination`을 입력으로 받는다.
- Spring Data `Page`/`Pageable`은 repository 또는 query adapter 내부 구현으로만 둔다.
- API response는 `{Feature}PageResult` 또는 page response DTO로 변환해서 반환한다.

## Module Rule

- JPA entity가 있는 `domain` 모듈에서 QueryDSL annotation processing으로 `Q` metamodel을 생성한다.
- domain source code는 `com.querydsl.*`를 import하지 않는다. 생성된 `Q` 파일은 build artifact로만 취급한다.
- QueryDSL query 작성은 `infra` 모듈의 `QueryDsl{Feature}QueryAdapter`에서만 한다.
- `application`은 query port/result만 알고 QueryDSL 타입을 알지 않는다.

## Adapter Rule

- QueryDSL adapter는 generated `Q` 타입을 import해서 사용한다.
- `PathBuilder`는 임시 우회 구현으로 간주하고 사용하지 않는다.
- 조회 필드가 많거나 entity 전체 로딩이 불필요하면 infra 전용 projection DTO를 만들고 `@QueryProjection`을 사용한다.
- projection DTO는 `infra` 모듈의 QueryDSL adapter 근처에 별도 파일로 둔다. `QueryDsl{Feature}QueryAdapter.kt` 안에 `@QueryProjection` row DTO를 같이 선언하지 않는다.
- projection DTO는 application/domain 타입으로 노출하지 않는다.
- projection DTO는 query row를 표현하고, application `Result`/`PageResult`로 변환해서 port 밖으로 내보낸다.
- entity 전체가 필요한 경우가 아니면 `selectFrom(entity)`보다 `select(QProjection(...))`를 우선한다.
- `Predicate`, `where`, `condition` 같은 불투명 조건 파라미터로 조회 의도를 숨기지 않는다. 회원별 조회, 주문별 조회처럼 의미가 다른 조건은 각각 명시적인 메서드/query block으로 작성한다.
- `select(...)` 안의 projection 필드는 쿼리 블록에서 바로 보이게 작성한다. `couponHistoryRow()`처럼 select 필드를 숨기는 helper를 만들지 않는다.
- 중복 제거는 page result 조립, pagination 계산처럼 query field 의미가 사라지지 않는 helper에만 적용한다.
- `JPAQueryFactory`는 adapter 내부에서 주입받은 `EntityManager`로 생성하거나 infra config bean으로 주입받는다.
- 목록 조회는 공통 `Pagination`의 normalized `page`, `size`, `sort`를 사용한다.
- memberId, orderId 같은 식별자는 `Pagination`에 넣지 않고 query adapter 메서드 파라미터로 분리한다.
- QueryDSL 목록 조회는 content query와 count query를 분리하고 `PageableExecutionUtils.getPage(content, pageable) { countQuery }`로 page metadata를 만든다.
- Spring Data `Page`/`Pageable`은 좁은 repository 계약 또는 query adapter 내부 구현 디테일로만 사용하고 required use case/controller/API response 밖으로 노출하지 않는다.
- page response는 `items`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`를 포함한다.
- total count query는 목록 query와 같은 where 조건을 사용한다.
- sort field는 허용된 필드만 노출한다. 현재 기본 정렬은 `id` 기준 asc/desc만 허용한다.

## Spring Data Pageable Example

단순 단일 aggregate 목록은 Spring Data `Pageable`을 사용할 수 있다. 단, `Page`를 use case 밖으로 그대로 내보내지 않는다.

```kotlin
interface MemberRepository : Repository<Member, Long> {
    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<Member>
}

@Transactional(readOnly = true)
override fun listMembers(pagination: Pagination): MemberPageResult {
    val page = memberRepository.findAllByDeletedAtIsNull(pagination.toPageable())

    return MemberPageResult(
        items = page.content.map { member ->
            MemberSummaryResult(
                id = member.id,
                name = member.name,
                email = member.email,
            )
        },
        page = page.number,
        size = page.size,
        totalElements = page.totalElements,
        totalPages = page.totalPages,
        hasNext = page.hasNext(),
    )
}

private fun Pagination.toPageable(): Pageable =
    PageRequest.of(
        normalizedPage,
        normalizedSize,
        Sort.by(sortDirection.toSpringDirection(), "id"),
    )

private fun SortDirection.toSpringDirection(): Sort.Direction =
    when (this) {
        SortDirection.ASC -> Sort.Direction.ASC
        SortDirection.DESC -> Sort.Direction.DESC
    }
```

위 예시는 허용된다. 조회 조건이 `memberId`, `status`, 기간, 키워드, join, projection으로 커지면 QueryDSL adapter로 옮긴다.

## QueryDSL Pagination Example

여러 조건과 projection이 있는 목록은 QueryDSL을 사용하고, content query와 count query를 분리한다.

```kotlin
fun listMemberCouponHistories(memberId: Long, pagination: Pagination): CouponHistoryPageResult {
    val pageable = pagination.toPageable()

    val content = queryFactory
        .select(
            QCouponHistoryRow(
                couponHistory.id,
                couponHistory.couponId,
                couponHistory.memberId,
                couponHistory.orderId,
                couponHistory.paymentId,
                couponHistory.type,
            )
        )
        .from(couponHistory)
        .where(couponHistory.memberId.eq(memberId))
        .orderBy(couponHistory.id.toOrderSpecifier(pagination.sortDirection))
        .offset(pageable.offset)
        .limit(pageable.pageSize.toLong())
        .fetch()

    val countQuery = queryFactory
        .select(couponHistory.id.count())
        .from(couponHistory)
        .where(couponHistory.memberId.eq(memberId))

    val page = PageableExecutionUtils.getPage(content, pageable) {
        countQuery.fetchOne() ?: 0L
    }

    return page.toCouponHistoryPageResult()
}
```

## Forbidden

```kotlin
// 금지: generated Q 타입 대신 문자열 path로 조회
PathBuilder(Coupon::class.java, "coupon")

// 금지: controller/service에서 QueryDSL 직접 호출
JPAQueryFactory(entityManager)

// 금지: 조회 의미를 숨기는 조건 파라미터
private fun searchHistories(where: Predicate)

// 금지: Spring Data Page를 API/use case 경계 밖으로 그대로 노출
fun listMembers(pageable: Pageable): Page<Member>
```

## Projection Example

```kotlin
data class CouponRow @QueryProjection constructor(
    val id: Long,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val status: CouponStatus,
)

queryFactory
    .select(QCouponRow(coupon.id, coupon.memberId, coupon.orderId, coupon.paymentId, coupon.status))
    .from(coupon)
    .where(coupon.memberId.eq(memberId))
    .fetch()
```

## Test Rule

- QueryDSL adapter는 infra integration test로 limit, offset, sort, count 조건을 검증한다.
- 대량 조회 endpoint는 `ApiResponse<List<T>>`를 반환하지 않고 page result를 반환한다.
