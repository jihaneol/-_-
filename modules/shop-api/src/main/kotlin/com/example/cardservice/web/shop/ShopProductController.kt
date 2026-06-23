package com.example.cardservice.web.shop

import com.example.cardservice.application.commerce.response.ProductResponse
import com.example.cardservice.application.commerce.response.ProductListResponse
import com.example.cardservice.application.commerce.required.ProductQueryUseCase
import com.example.cardservice.application.commerce.response.toResponse
import com.example.cardservice.web.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/shop/products")
@Tag(name = "Shop Product", description = "쇼핑몰 상품 API")
class ShopProductController(
    private val productQueryUseCase: ProductQueryUseCase,
) {
    @GetMapping
    @Operation(summary = "판매 상품 목록 조회")
    fun listProducts(): ApiResponse<ProductListResponse> =
        ApiResponse.success(ProductListResponse(productQueryUseCase.listProducts().map { it.toResponse() }))

    @GetMapping("/{productId}")
    @Operation(summary = "판매 상품 상세 조회")
    fun getProduct(@PathVariable productId: Long): ApiResponse<ProductResponse> =
        ApiResponse.success(productQueryUseCase.getProduct(productId).toResponse())
}
