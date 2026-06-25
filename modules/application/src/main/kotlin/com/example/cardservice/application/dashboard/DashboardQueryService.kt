package com.example.cardservice.application.dashboard

import com.example.cardservice.application.order.provided.OrderRepository
import com.example.cardservice.application.coupon.provided.CouponRepository
import com.example.cardservice.application.member.provided.MemberRepository
import com.example.cardservice.application.product.provided.ProductRepository
import com.example.cardservice.application.dashboard.required.DashboardQueryUseCase
import com.example.cardservice.domain.coupon.CouponStatus
import com.example.cardservice.domain.order.OrderStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DashboardQueryService(
    private val memberRepository: MemberRepository,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val couponRepository: CouponRepository,
) : DashboardQueryUseCase {
    @Transactional(readOnly = true)
    override fun getSummary(): DashboardSummaryResult =
        DashboardSummaryResult(
            memberCount = memberRepository.countByDeletedAtIsNull(),
            productCount = productRepository.countByDeletedAtIsNull(),
            orderCount = orderRepository.countByDeletedAtIsNull(),
            paidOrderCount = orderRepository.countByStatusAndDeletedAtIsNull(OrderStatus.PAID),
            refundedOrderCount = orderRepository.countByStatusAndDeletedAtIsNull(OrderStatus.REFUNDED),
            issuedCouponCount = couponRepository.countByStatus(CouponStatus.ISSUED),
        )
}
