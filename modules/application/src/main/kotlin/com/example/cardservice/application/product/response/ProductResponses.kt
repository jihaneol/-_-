package com.example.cardservice.application.product.response

import com.example.cardservice.application.product.ProductPageResponse as ProductUseCasePageResponse
import com.example.cardservice.application.product.ProductResponse as ProductUseCaseResponse
import com.example.cardservice.domain.product.ProductSaleStatus

data class ProductApiResponse(
    val id: Long,
    val name: String,
    val price: Long,
    val saleStatus: ProductSaleStatus,
    val couponAccrualCount: Long,
    val exchangeEligible: Boolean,
)

data class ProductApiPageResponse(
    val items: List<ProductApiResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

fun ProductUseCaseResponse.toApiResponse(): ProductApiResponse =
    ProductApiResponse(
        id = id,
        name = name,
        price = price,
        saleStatus = saleStatus,
        couponAccrualCount = price / 5_000L,
        exchangeEligible = price == 5_000L && saleStatus == ProductSaleStatus.ON_SALE,
    )

fun ProductUseCasePageResponse.toApiResponse(): ProductApiPageResponse =
    ProductApiPageResponse(
        items = items.map { it.toApiResponse() },
        page = page,
        size = size,
        totalElements = totalElements,
        totalPages = totalPages,
        hasNext = hasNext,
    )
