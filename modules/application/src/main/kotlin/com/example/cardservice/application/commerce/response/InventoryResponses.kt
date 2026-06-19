package com.example.cardservice.application.commerce.response

import com.example.cardservice.application.commerce.InventoryResult

data class InventoryResponse(val id: Long, val productId: Long, val quantity: Long)

fun InventoryResult.toResponse(): InventoryResponse = InventoryResponse(id, productId, quantity)
