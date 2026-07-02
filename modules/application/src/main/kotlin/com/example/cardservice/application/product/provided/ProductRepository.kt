package com.example.cardservice.application.product.provided

import com.example.cardservice.domain.product.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.Repository

/**
 * Product entity 저장과 기본 조회를 위해 application layer가 정의하는 Spring Data repository 계약이다.
 */
interface ProductRepository : Repository<Product, Long> {
    fun save(product: Product): Product
    fun findAllByDeletedAtIsNull(pageable: Pageable): Page<Product>
    fun findByIdAndDeletedAtIsNull(id: Long): Product?
}
