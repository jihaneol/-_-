package com.example.cardservice.application.commerce.required

import com.example.cardservice.application.commerce.CreateProductInput
import com.example.cardservice.application.commerce.ProductResult
import com.example.cardservice.application.commerce.UpdateProductInput

/**
 * 상품 변경 요청을 application layer로 전달하는 inbound port다.
 */
interface ProductUseCase {
    fun createProduct(input: CreateProductInput): ProductResult
    fun updateProduct(productId: Long, input: UpdateProductInput): ProductResult
    fun deleteProduct(productId: Long)
}

/**
 * 상품 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface ProductQueryUseCase {
    fun listProducts(): List<ProductResult>
    fun getProduct(productId: Long): ProductResult
}
