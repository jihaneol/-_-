package com.example.cardservice.web.common

data class ApiErrorResponse(
    val code: String,
    val message: String,
    val fields: List<ApiFieldErrorResponse> = emptyList(),
)

data class ApiFieldErrorResponse(
    val field: String,
    val message: String,
)

