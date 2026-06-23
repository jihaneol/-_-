package com.example.cardservice.application.commerce

import com.example.cardservice.application.commerce.provided.CommerceLockPort
import com.example.cardservice.application.commerce.provided.CouponHistoryRepository
import com.example.cardservice.application.commerce.provided.CouponRepository
import com.example.cardservice.application.commerce.provided.InventoryRepository
import com.example.cardservice.application.commerce.provided.MemberRepository
import com.example.cardservice.application.commerce.provided.ProductRepository
import com.example.cardservice.application.commerce.required.CouponExchangeUseCase
import com.example.cardservice.domain.commerce.model.coupon.Coupon
import com.example.cardservice.domain.commerce.model.coupon.CouponHistory
import com.example.cardservice.domain.commerce.model.coupon.CouponStatus
import com.example.cardservice.domain.commerce.model.product.Product
import com.example.cardservice.domain.commerce.model.product.ProductSaleStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 쿠폰 교환 상태 변경과 히스토리 기록을 한 트랜잭션으로 조율하는 application command service다.
 */
@Service
class CouponExchangeService(
    private val commerceLockPort: CommerceLockPort,
    private val memberRepository: MemberRepository,
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository,
    private val couponRepository: CouponRepository,
    private val couponHistoryRepository: CouponHistoryRepository,
) : CouponExchangeUseCase {
    @Transactional
    override fun exchangeCoupon(couponId: Long): CouponExchangeResult {
        val coupon = loadCouponForUpdate(couponId)
        coupon.exchange()
        val savedCoupon = couponRepository.saveAll(listOf(coupon)).single()
        val history = couponHistoryRepository.saveAll(listOf(CouponHistory.exchanged(savedCoupon))).single()
        return CouponExchangeResult(coupon = savedCoupon.toResult(), history = history.toResult())
    }

    @Transactional
    override fun approveCouponExchange(memberId: Long, input: ApproveCouponExchangeInput): ApproveCouponExchangeResult {
        memberRepository.findByIdAndDeletedAtIsNull(memberId) ?: throw IllegalArgumentException("회원을 찾을 수 없습니다.")
        val product = loadExchangeProduct(input.productId)
        val inventory = commerceLockPort.loadInventoryForUpdate(input.productId) ?: throw IllegalArgumentException("재고를 찾을 수 없습니다.")
        val coupons = commerceLockPort.loadIssuedCouponsForExchange(memberId, REQUIRED_COUPON_COUNT)
        require(coupons.size == REQUIRED_COUPON_COUNT) { "사용 가능한 쿠폰이 부족합니다." }

        inventory.decrease(EXCHANGE_PRODUCT_QUANTITY)
        coupons.forEach { it.exchange() }

        inventoryRepository.save(inventory)
        val savedCoupons = couponRepository.saveAll(coupons)
        couponHistoryRepository.saveAll(savedCoupons.map { CouponHistory.exchanged(it) })

        return ApproveCouponExchangeResult(
            memberId = memberId,
            productId = requireNotNull(product.id),
            productName = product.name,
            exchangedCouponCount = savedCoupons.size,
            remainingIssuedCouponCount = couponRepository.countByMemberIdAndStatus(memberId, CouponStatus.ISSUED),
            exchangedCouponIds = savedCoupons.map { requireNotNull(it.id) },
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
