package com.example.cardservice.application.product

import com.example.cardservice.domain.product.ProductSaleStatus

data class ProductResponse(val id: Long, val name: String, val price: Long, val saleStatus: ProductSaleStatus)

data class ProductPageResponse(
    val items: List<ProductResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)
