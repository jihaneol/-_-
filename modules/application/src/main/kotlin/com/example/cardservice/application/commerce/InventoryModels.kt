package com.example.cardservice.application.commerce

data class CreateInventoryInput(val productId: Long, val quantity: Long)
data class AdjustInventoryInput(val quantity: Long)
data class InventoryResult(val id: Long, val productId: Long, val quantity: Long)
