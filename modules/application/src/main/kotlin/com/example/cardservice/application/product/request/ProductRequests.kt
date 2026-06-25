package com.example.cardservice.application.product.request

import com.example.cardservice.domain.product.ProductSaleStatus

data class ProductCreateRequest(
    val name: String,
    val price: Long,
)

data class ProductUpdateRequest(
    val name: String,
    val price: Long,
    val saleStatus: ProductSaleStatus,
)
