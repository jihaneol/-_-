package com.example.cardservice.application.commerce.response

import com.example.cardservice.application.commerce.ProductPageResult
import com.example.cardservice.application.commerce.ProductResult
import com.example.cardservice.domain.commerce.model.product.ProductSaleStatus

data class ProductResponse(
    val id: Long,
    val name: String,
    val price: Long,
    val saleStatus: ProductSaleStatus,
    val couponAccrualCount: Long,
    val exchangeEligible: Boolean,
)

data class ProductPageResponse(
    val items: List<ProductResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

fun ProductResult.toResponse(): ProductResponse =
    ProductResponse(
        id = id,
        name = name,
        price = price,
        saleStatus = saleStatus,
        couponAccrualCount = price / 5_000L,
        exchangeEligible = price == 5_000L && saleStatus == ProductSaleStatus.ON_SALE,
    )

fun ProductPageResult.toResponse(): ProductPageResponse =
    ProductPageResponse(
        items = items.map { it.toResponse() },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
        hasNext = hasNext,
    )
