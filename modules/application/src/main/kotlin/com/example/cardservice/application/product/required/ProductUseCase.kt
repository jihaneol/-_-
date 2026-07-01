package com.example.cardservice.application.product.required

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.product.CreateProductRequest
import com.example.cardservice.application.product.ProductPageResponse
import com.example.cardservice.application.product.ProductResponse
import com.example.cardservice.application.product.UpdateProductRequest

/**
 * 상품 변경 요청을 application layer로 전달하는 inbound port다.
 */
interface ProductUseCase {
    fun createProduct(input: CreateProductRequest): ProductResponse
    fun updateProduct(request: UpdateProductRequest): ProductResponse
    fun deleteProduct(productId: Long)
}

/**
 * 상품 조회 요청을 application layer로 전달하는 inbound port다.
 */
interface ProductQueryUseCase {
    fun listProducts(pagination: Pagination): ProductPageResponse
    fun getProduct(productId: Long): ProductResponse
}
