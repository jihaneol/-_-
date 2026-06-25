package com.example.cardservice.application.product

import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.common.toPageable
import com.example.cardservice.application.product.provided.ProductRepository
import com.example.cardservice.application.product.required.ProductQueryUseCase
import com.example.cardservice.domain.product.Product
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 상품 조회 흐름을 조율하는 application query service다.
 */
@Service
class ProductQueryService(
    private val productRepository: ProductRepository,
) : ProductQueryUseCase {
    @Transactional(readOnly = true)
    override fun listProducts(pagination: Pagination): ProductPageResult =
        productRepository.findAllByDeletedAtIsNull(pagination.toPageable()).toPageResult()

    @Transactional(readOnly = true)
    override fun getProduct(productId: Long): ProductResult =
        (productRepository.findByIdAndDeletedAtIsNull(productId) ?: throw IllegalArgumentException("상품을 찾을 수 없습니다."))
            .toResult()

    private fun Page<Product>.toPageResult(): ProductPageResult =
        ProductPageResult(
            items = content.map { it.toResult() },
            page = number,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = hasNext(),
        )
}
