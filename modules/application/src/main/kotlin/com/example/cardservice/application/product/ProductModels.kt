package com.example.cardservice.application.product

import com.example.cardservice.domain.product.ProductSaleStatus

data class CreateProductInput(val name: String, val price: Long)
data class UpdateProductInput(val name: String, val price: Long, val saleStatus: ProductSaleStatus)
data class ProductResult(val id: Long, val name: String, val price: Long, val saleStatus: ProductSaleStatus)
data class ProductPageResult(
    val items: List<ProductResult>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)
