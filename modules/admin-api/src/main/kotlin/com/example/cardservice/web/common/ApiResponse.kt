package com.example.cardservice.web.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

data class ApiResponse<T>(
    val code: String,
    val message: String,
    val data: T,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> =
            ApiResponse(
                code = "SUCCESS",
                message = "요청이 성공했습니다.",
                data = data,
            )
    }
}

fun <T> ok(data: T): ResponseEntity<ApiResponse<T>> =
    ResponseEntity.ok(ApiResponse.success(data))

fun <T> created(data: T): ResponseEntity<ApiResponse<T>> =
    ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data))
