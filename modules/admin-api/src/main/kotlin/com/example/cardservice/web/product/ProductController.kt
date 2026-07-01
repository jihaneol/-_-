package com.example.cardservice.web.product

import com.example.cardservice.application.common.DEFAULT_PAGE_SIZE
import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.product.CreateProductRequest
import com.example.cardservice.application.product.UpdateProductRequest
import com.example.cardservice.application.product.response.ProductApiPageResponse
import com.example.cardservice.application.product.response.ProductApiResponse
import com.example.cardservice.application.product.required.ProductQueryUseCase
import com.example.cardservice.application.product.required.ProductUseCase
import com.example.cardservice.application.product.response.toApiResponse
import com.example.cardservice.web.common.ApplicationResponseType
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.toApplicationResponse
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
import org.springframework.web.bind.annotation.RequestParam
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
    fun createProduct(@RequestBody request: CreateProductRequest): ResponseEntity<ApiResponse<ProductApiResponse>> =
        productUseCase
            .createProduct(request)
            .toApiResponse()
            .toApplicationResponse(ApplicationResponseType.CREATED)

    @GetMapping
    @Operation(summary = "상품 목록 조회")
    fun listProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "$DEFAULT_PAGE_SIZE") size: Int,
        @RequestParam(defaultValue = "id,desc") sort: String,
    ): ResponseEntity<ApiResponse<ProductApiPageResponse>> =
        productQueryUseCase.listProducts(Pagination(page, size, sort)).toApiResponse().toApplicationResponse()

    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회")
    fun getProduct(@PathVariable productId: Long): ResponseEntity<ApiResponse<ProductApiResponse>> =
        productQueryUseCase.getProduct(productId).toApiResponse().toApplicationResponse()

    @PatchMapping("/{productId}")
    @Operation(summary = "상품 수정")
    fun updateProduct(
        @PathVariable productId: Long,
        @RequestBody request: UpdateProductRequest,
    ): ResponseEntity<ApiResponse<ProductApiResponse>> =
        productUseCase
            .updateProduct(request.copy().also { it.productId = productId })
            .toApiResponse()
            .toApplicationResponse()

    @DeleteMapping("/{productId}")
    @Operation(summary = "상품 소프트 삭제")
    fun deleteProduct(@PathVariable productId: Long): ResponseEntity<ApiResponse<Unit>> {
        productUseCase.deleteProduct(productId)
        return Unit.toApplicationResponse(ApplicationResponseType.NO_CONTENT)
    }
}
