package com.example.cardservice.web.common

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
