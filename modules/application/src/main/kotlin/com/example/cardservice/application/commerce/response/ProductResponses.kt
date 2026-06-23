package com.example.cardservice.application.commerce.response

import com.example.cardservice.application.commerce.ProductResult
import com.example.cardservice.domain.commerce.model.ProductSaleStatus

data class ProductResponse(
    val id: Long,
    val name: String,
    val price: Long,
    val saleStatus: ProductSaleStatus,
    val couponAccrualCount: Long,
    val exchangeEligible: Boolean,
)

data class ProductListResponse(val products: List<ProductResponse>)

fun ProductResult.toResponse(): ProductResponse =
    ProductResponse(
        id = id,
        name = name,
        price = price,
        saleStatus = saleStatus,
        couponAccrualCount = price / 5_000L,
        exchangeEligible = price == 5_000L && saleStatus == ProductSaleStatus.ON_SALE,
    )
