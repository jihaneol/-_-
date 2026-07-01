package com.example.cardservice.application.product

import com.example.cardservice.application.product.provided.ProductRepository
import com.example.cardservice.application.product.required.ProductUseCase
import com.example.cardservice.domain.product.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 상품 생성, 수정, 삭제 흐름을 조율하는 application service다.
 */
@Service
class ProductService(
    private val productRepository: ProductRepository,
) : ProductUseCase {
    @Transactional
    override fun createProduct(input: CreateProductRequest): ProductResponse =
        productRepository.save(Product.create(input.name, input.price)).toResponse()

    @Transactional
    override fun updateProduct(request: UpdateProductRequest): ProductResponse {
        val product = loadProduct(request.productId)
        product.update(request.name, request.price, request.saleStatus)
        return productRepository.save(product).toResponse()
    }

    @Transactional
    override fun deleteProduct(productId: Long) {
        val product = loadProduct(productId)
        product.softDelete()
        productRepository.save(product)
    }

    private fun loadProduct(productId: Long): Product =
        productRepository.findByIdAndDeletedAtIsNull(productId) ?: throw IllegalArgumentException("상품을 찾을 수 없습니다.")
}

internal fun Product.toResponse(): ProductResponse =
    ProductResponse(id = id, name = name, price = price, saleStatus = saleStatus)
