package com.example.cardservice.application.inventory

import com.fasterxml.jackson.annotation.JsonIgnore

data class CreateInventoryRequest(val quantity: Long) {
    @get:JsonIgnore
    var productId: Long = 0L
}

data class AdjustInventoryRequest(val quantity: Long) {
    @get:JsonIgnore
    var productId: Long = 0L
}
