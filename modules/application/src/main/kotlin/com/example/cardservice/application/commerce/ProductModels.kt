package com.example.cardservice.application.commerce

import com.example.cardservice.domain.commerce.model.ProductSaleStatus

data class CreateProductInput(val name: String, val price: Long)
data class UpdateProductInput(val name: String, val price: Long, val saleStatus: ProductSaleStatus)
data class ProductResult(val id: Long, val name: String, val price: Long, val saleStatus: ProductSaleStatus)
