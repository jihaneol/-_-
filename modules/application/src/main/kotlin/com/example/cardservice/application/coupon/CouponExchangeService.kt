package com.example.cardservice.application.coupon

import com.example.cardservice.application.order.provided.OrderWorkflowLockPort
import com.example.cardservice.application.coupon.provided.CouponHistoryRepository
import com.example.cardservice.application.coupon.provided.CouponRepository
import com.example.cardservice.application.inventory.provided.InventoryRepository
import com.example.cardservice.application.member.provided.MemberRepository
import com.example.cardservice.application.product.provided.ProductRepository
import com.example.cardservice.application.coupon.required.CouponExchangeUseCase
import com.example.cardservice.domain.coupon.Coupon
import com.example.cardservice.domain.coupon.CouponHistory
import com.example.cardservice.domain.coupon.CouponStatus
import com.example.cardservice.domain.product.Product
import com.example.cardservice.domain.product.ProductSaleStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 쿠폰 교환 상태 변경과 히스토리 기록을 한 트랜잭션으로 조율하는 application command service다.
 */
@Service
class CouponExchangeService(
    private val commerceLockPort: OrderWorkflowLockPort,
    private val memberRepository: MemberRepository,
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository,
    private val couponRepository: CouponRepository,
    private val couponHistoryRepository: CouponHistoryRepository,
) : CouponExchangeUseCase {
    @Transactional
    override fun exchangeCoupon(couponId: Long): CouponExchangeResponse {
        val coupon = loadCouponForUpdate(couponId)
        coupon.exchange()
        val savedCoupon = couponRepository.saveAll(listOf(coupon)).single()
        val history = couponHistoryRepository.saveAll(listOf(CouponHistory.exchanged(savedCoupon))).single()
        return CouponExchangeResponse(coupon = savedCoupon.toResponse(), history = history.toResponse())
    }

    @Transactional
    override fun approveCouponExchange(request: ApproveCouponExchangeRequest): ApproveCouponExchangeResponse {
        val memberId = request.memberId
        memberRepository.findByIdAndDeletedAtIsNull(memberId) ?: throw IllegalArgumentException("회원을 찾을 수 없습니다.")
        val product = loadExchangeProduct(request.productId)
        val inventory = commerceLockPort.loadInventoryForUpdate(request.productId) ?: throw IllegalArgumentException("재고를 찾을 수 없습니다.")
        val coupons = commerceLockPort.loadIssuedCouponsForExchange(memberId, REQUIRED_COUPON_COUNT)
        require(coupons.size == REQUIRED_COUPON_COUNT) { "사용 가능한 쿠폰이 부족합니다." }

        inventory.decrease(EXCHANGE_PRODUCT_QUANTITY)
        coupons.forEach { it.exchange() }

        inventoryRepository.save(inventory)
        val savedCoupons = couponRepository.saveAll(coupons)
        couponHistoryRepository.saveAll(savedCoupons.map { CouponHistory.exchanged(it) })

        return ApproveCouponExchangeResponse(
            memberId = memberId,
            productId = product.id,
            productName = product.name,
            exchangedCouponCount = savedCoupons.size,
            remainingIssuedCouponCount = couponRepository.countByMemberIdAndStatus(memberId, CouponStatus.ISSUED),
            exchangedCouponIds = savedCoupons.map { it.id },
        )
    }

    private fun loadCouponForUpdate(couponId: Long): Coupon =
        commerceLockPort.loadCouponForUpdate(couponId) ?: throw IllegalArgumentException("쿠폰을 찾을 수 없습니다.")

    private fun loadExchangeProduct(productId: Long): Product {
        val product = productRepository.findByIdAndDeletedAtIsNull(productId) ?: throw IllegalArgumentException("상품을 찾을 수 없습니다.")
        require(product.saleStatus == ProductSaleStatus.ON_SALE) { "판매 중인 상품만 교환할 수 있습니다." }
        require(product.price == EXCHANGE_PRODUCT_PRICE) { "5,000원 상품만 교환할 수 있습니다." }
        return product
    }

    private companion object {
        const val REQUIRED_COUPON_COUNT = 10
        const val EXCHANGE_PRODUCT_QUANTITY = 1L
        const val EXCHANGE_PRODUCT_PRICE = 5_000L
    }
}
