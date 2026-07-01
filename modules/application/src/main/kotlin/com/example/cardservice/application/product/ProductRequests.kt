package com.example.cardservice.application.product

import com.example.cardservice.domain.product.ProductSaleStatus

data class CreateProductRequest(val name: String, val price: Long)

data class UpdateProductRequest(val name: String, val price: Long, val saleStatus: ProductSaleStatus) {
    var productId: Long = 0L
}
