package com.example.cardservice.web.common

data class ApiErrorResponse(
    val result: ApplicationResult,
    val fields: List<ApiFieldErrorResponse> = emptyList(),
)

data class ApiFieldErrorResponse(
    val field: String,
    val message: String,
)
