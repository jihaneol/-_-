package com.example.cardservice.application.commerce

import com.example.cardservice.application.commerce.provided.CommerceOrderRepository
import com.example.cardservice.application.commerce.provided.CouponRepository
import com.example.cardservice.application.commerce.provided.MemberRepository
import com.example.cardservice.application.commerce.provided.ProductRepository
import com.example.cardservice.application.commerce.required.DashboardQueryUseCase
import com.example.cardservice.domain.commerce.model.coupon.CouponStatus
import com.example.cardservice.domain.commerce.model.order.OrderStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DashboardQueryService(
    private val memberRepository: MemberRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: CommerceOrderRepository,
    private val couponRepository: CouponRepository,
) : DashboardQueryUseCase {
    @Transactional(readOnly = true)
    override fun getSummary(): CommerceDashboardSummaryResult =
        CommerceDashboardSummaryResult(
            memberCount = memberRepository.countByDeletedAtIsNull(),
            productCount = productRepository.countByDeletedAtIsNull(),
            orderCount = orderRepository.countByDeletedAtIsNull(),
            paidOrderCount = orderRepository.countByStatusAndDeletedAtIsNull(OrderStatus.PAID),
            refundedOrderCount = orderRepository.countByStatusAndDeletedAtIsNull(OrderStatus.REFUNDED),
            issuedCouponCount = couponRepository.countByStatus(CouponStatus.ISSUED),
        )
}
