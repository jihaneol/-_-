package com.example.cardservice.web.commerce

import com.example.cardservice.application.commerce.CreateProductInput
import com.example.cardservice.application.commerce.UpdateProductInput
import com.example.cardservice.application.commerce.response.ProductListResponse
import com.example.cardservice.application.commerce.response.ProductResponse
import com.example.cardservice.application.commerce.request.ProductCreateRequest
import com.example.cardservice.application.commerce.request.ProductUpdateRequest
import com.example.cardservice.application.commerce.required.ProductQueryUseCase
import com.example.cardservice.application.commerce.required.ProductUseCase
import com.example.cardservice.application.commerce.response.toResponse
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.created
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/products")
@Tag(name = "Product", description = "상품 운영 API")
class ProductController(
    private val productUseCase: ProductUseCase,
    private val productQueryUseCase: ProductQueryUseCase,
) {
    @PostMapping
    @Operation(summary = "상품 생성")
    fun createProduct(@RequestBody request: ProductCreateRequest): ResponseEntity<ApiResponse<ProductResponse>> =
        created(productUseCase.createProduct(CreateProductInput(request.name, request.price)).toResponse())

    @GetMapping
    @Operation(summary = "상품 목록 조회")
    fun listProducts(): ApiResponse<ProductListResponse> =
        ApiResponse.success(ProductListResponse(productQueryUseCase.listProducts().map { it.toResponse() }))

    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회")
    fun getProduct(@PathVariable productId: Long): ApiResponse<ProductResponse> =
        ApiResponse.success(productQueryUseCase.getProduct(productId).toResponse())

    @PatchMapping("/{productId}")
    @Operation(summary = "상품 수정")
    fun updateProduct(
        @PathVariable productId: Long,
        @RequestBody request: ProductUpdateRequest,
    ): ApiResponse<ProductResponse> =
        ApiResponse.success(productUseCase.updateProduct(productId, UpdateProductInput(request.name, request.price, request.saleStatus)).toResponse())

    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 소프트 삭제")
    fun deleteProduct(@PathVariable productId: Long): ResponseEntity<Void> {
        productUseCase.deleteProduct(productId)
        return ResponseEntity.noContent().build()
    }
}
