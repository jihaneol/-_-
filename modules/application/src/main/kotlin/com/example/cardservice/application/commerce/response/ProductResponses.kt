package com.example.cardservice.application.commerce.response

import com.example.cardservice.application.commerce.ProductResult
import com.example.cardservice.domain.commerce.model.ProductSaleStatus

data class ProductResponse(val id: Long, val name: String, val price: Long, val saleStatus: ProductSaleStatus)

fun ProductResult.toResponse(): ProductResponse = ProductResponse(id, name, price, saleStatus)
