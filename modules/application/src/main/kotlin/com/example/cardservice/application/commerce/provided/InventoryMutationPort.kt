package com.example.cardservice.application.commerce.provided

/**
 * 결제 경로에서 재고 row를 오래 잠그지 않고 조건부 수량 변경을 수행하는 outbound port다.
 */
interface InventoryMutationPort {
    fun decreaseQuantityIfEnough(productId: Long, quantity: Long): Boolean
    fun increaseQuantity(productId: Long, quantity: Long): Boolean
}
