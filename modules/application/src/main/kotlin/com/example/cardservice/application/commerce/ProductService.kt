package com.example.cardservice.application.commerce

import com.example.cardservice.application.commerce.provided.ProductRepository
import com.example.cardservice.application.commerce.required.ProductQueryUseCase
import com.example.cardservice.application.commerce.required.ProductUseCase
import com.example.cardservice.domain.commerce.model.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 상품 생성, 수정, 삭제와 조회 흐름을 조율하는 application service다.
 */
@Service
class ProductService(
    private val productRepository: ProductRepository,
) : ProductUseCase, ProductQueryUseCase {
    @Transactional
    override fun createProduct(input: CreateProductInput): ProductResult =
        productRepository.save(Product.create(input.name, input.price)).toResult()

    @Transactional
    override fun updateProduct(productId: Long, input: UpdateProductInput): ProductResult {
        val product = loadProduct(productId)
        product.update(input.name, input.price, input.saleStatus)
        return productRepository.save(product).toResult()
    }

    @Transactional
    override fun deleteProduct(productId: Long) {
        val product = loadProduct(productId)
        product.softDelete()
        productRepository.save(product)
    }

    @Transactional(readOnly = true)
    override fun listProducts(): List<ProductResult> =
        productRepository.findAllByDeletedAtIsNull().map { it.toResult() }

    @Transactional(readOnly = true)
    override fun getProduct(productId: Long): ProductResult =
        loadProduct(productId).toResult()

    private fun loadProduct(productId: Long): Product =
        productRepository.findByIdAndDeletedAtIsNull(productId) ?: throw IllegalArgumentException("상품을 찾을 수 없습니다.")
}

internal fun Product.toResult(): ProductResult =
    ProductResult(id = requireNotNull(id), name = name, price = price, saleStatus = saleStatus)
