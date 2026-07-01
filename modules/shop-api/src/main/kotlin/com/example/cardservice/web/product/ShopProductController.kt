package com.example.cardservice.web.product

import com.example.cardservice.application.common.DEFAULT_PAGE_SIZE
import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.product.required.ProductQueryUseCase
import com.example.cardservice.application.product.response.ProductApiPageResponse
import com.example.cardservice.application.product.response.ProductApiResponse
import com.example.cardservice.application.product.response.toApiResponse
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.toApplicationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/shop/products")
@Tag(name = "Shop Product", description = "쇼핑몰 상품 API")
class ShopProductController(
    private val productQueryUseCase: ProductQueryUseCase,
) {
    @GetMapping
    @Operation(summary = "판매 상품 목록 조회")
    fun listProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "$DEFAULT_PAGE_SIZE") size: Int,
        @RequestParam(defaultValue = "id,desc") sort: String,
    ): ResponseEntity<ApiResponse<ProductApiPageResponse>> =
        productQueryUseCase.listProducts(Pagination(page, size, sort)).toApiResponse().toApplicationResponse()

    @GetMapping("/{productId}")
    @Operation(summary = "판매 상품 상세 조회")
    fun getProduct(@PathVariable productId: Long): ResponseEntity<ApiResponse<ProductApiResponse>> =
        productQueryUseCase.getProduct(productId).toApiResponse().toApplicationResponse()
}
