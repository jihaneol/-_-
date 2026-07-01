# QueryDSL Rule

QueryDSL은 조회 전용 read adapter 구현 기술이다. command service, domain entity, controller에서 직접 사용하지 않는다.

## Select Technology

| Case | Default |
|---|---|
| 단일 entity 단순 목록/상세 | Spring Data repository + `Pageable` 내부 사용 |
| join, 동적 조건, projection | QueryDSL |
| 운영자 대시보드, 정산/대사, batch 검증 화면 | QueryDSL |
| count 최적화가 필요한 대량 목록 | QueryDSL + `PageableExecutionUtils` |

```text
controller params -> Pagination -> query use case -> query port -> QueryDsl*QueryAdapter -> PageResponse
```

## Adapter Shape

```kotlin
@Component
class QueryDslCouponQueryAdapter(
    entityManager: EntityManager,
) : CouponQueryPort {
    private val queryFactory = JPAQueryFactory(entityManager)

    override fun searchCoupons(memberId: Long, pagination: Pagination): CouponPageResponse {
        val rows = queryFactory
            .select(QCouponRow(coupon.id, coupon.memberId, coupon.orderId, coupon.paymentId, coupon.status))
            .from(coupon)
            .where(coupon.memberId.eq(memberId))
            .orderBy(OrderSpecifier(orderOf(pagination.sortDirection), coupon.id))
            .offset(pagination.offset)
            .limit(pagination.normalizedSize.toLong())
            .fetch()

        val page = PageableExecutionUtils.getPage(rows, pagination.pageable()) {
            queryFactory.select(coupon.count()).from(coupon).where(coupon.memberId.eq(memberId)).fetchOne() ?: 0L
        }

        return page.toCouponPageResponse()
    }
}
```

Current example: `modules/infra/src/main/kotlin/com/example/cardservice/infra/coupon/QueryDslCouponQueryAdapter.kt`

## Projection

```kotlin
data class CouponRow @QueryProjection constructor(
    val id: Long,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val status: CouponStatus,
)
```

- generated `Q` 타입을 import한다. `PathBuilder`는 쓰지 않는다.
- row DTO는 adapter 근처 별도 파일에 둔다.
- row DTO는 application/domain 타입으로 노출하지 않고 Response/PageResponse로 변환한다.
- entity 전체가 필요 없으면 `selectFrom(entity)`보다 `select(QProjection(...))`.

## Pagination

```kotlin
fun searchMemberCouponHistories(memberId: Long, pagination: Pagination): CouponHistoryPageResponse
```

- controller는 `page`, `size`, `sort`만 받고 Spring `Pageable`을 직접 받지 않는다.
- use case/port는 공통 `Pagination`을 입력으로 받는다.
- `memberId`, `orderId`는 `Pagination`에 넣지 않고 메서드 파라미터로 분리한다.
- content query와 count query는 같은 where 조건을 사용한다.
- page response는 `items/page/size/totalElements/totalPages/hasNext`.
- sort field는 허용된 필드만 노출한다. 현재 기본은 `id` asc/desc.

## Spring Data Pageable Allowed

```kotlin
interface MemberRepository : Repository<Member, Long> {
    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<Member>
}

@Transactional(readOnly = true)
fun listMembers(pagination: Pagination): MemberPageResponse {
    val page = memberRepository.findAllByDeletedAtIsNull(pagination.toPageable())
    return page.toMemberPageResponse()
}
```

단순 단일 aggregate 조회에만 허용한다. 조건이 `status`, 기간, 키워드, join, projection으로 커지면 QueryDSL adapter로 옮긴다.

## Avoid

```kotlin
PathBuilder(Coupon::class.java, "coupon")       // 문자열 path
JPAQueryFactory(entityManager)                  // controller/service에서 직접 사용
private fun search(where: Predicate)            // 조회 의도 숨김
fun list(pageable: Pageable): Page<Member>      // use case/API 경계 노출
queryFactory.selectFrom(coupon).fetch()         // projection으로 충분한 조회
fun listAll(): List<CouponResponse>               // 무제한 top-level 목록
```

## Tests

- QueryDSL adapter는 infra integration test로 limit, offset, sort, count 조건을 검증한다.
- 대량 조회 endpoint는 `ApiResponse<List<T>>`가 아니라 page response를 반환한다.
