package com.example.cardservice.web.common

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

data class ApplicationResponse<T>(
    val result: ApplicationResult,
    val payload: T,
)

data class ApplicationResult(
    val code: String,
    val message: String,
)

enum class ApplicationResponseType(
    val httpStatus: HttpStatus,
    val code: String,
    val message: String,
) {
    OK(HttpStatus.OK, "OK", "요청이 성공했습니다."),
    CREATED(HttpStatus.CREATED, "OK", "요청이 성공했습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, "OK", "요청이 성공했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "요청 값이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "처리 중 오류가 발생했습니다."),
    ;

    fun toResult(message: String = this.message): ApplicationResult =
        ApplicationResult(code = code, message = message)
}

typealias ApiResponse<T> = ApplicationResponse<T>

fun <T> T.toApplicationResponse(
    type: ApplicationResponseType = ApplicationResponseType.OK,
): ResponseEntity<ApplicationResponse<T>> =
    when (type) {
        ApplicationResponseType.NO_CONTENT ->
            ResponseEntity.status(type.httpStatus).build()

        else ->
            ResponseEntity
                .status(type.httpStatus)
                .body(
                    ApplicationResponse(
                        result = type.toResult(),
                        payload = this,
                    ),
                )
    }
