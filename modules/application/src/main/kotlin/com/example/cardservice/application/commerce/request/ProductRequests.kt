package com.example.cardservice.application.commerce.request

import com.example.cardservice.domain.commerce.model.product.ProductSaleStatus

data class ProductCreateRequest(
    val name: String,
    val price: Long,
)

data class ProductUpdateRequest(
    val name: String,
    val price: Long,
    val saleStatus: ProductSaleStatus,
)
