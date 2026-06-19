package com.example.cardservice.web.common

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(exception: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val fields = exception.bindingResult.fieldErrors.map { fieldError ->
            ApiFieldErrorResponse(
                field = fieldError.field,
                message = fieldError.safeMessage(),
            )
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    code = "VALIDATION_ERROR",
                    message = "요청 값이 올바르지 않습니다.",
                    fields = fields,
                ),
            )
    }

    @ExceptionHandler(
        IllegalArgumentException::class,
        ConstraintViolationException::class,
        MethodArgumentTypeMismatchException::class,
    )
    fun handleBadRequestException(exception: Exception): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    code = "BAD_REQUEST",
                    message = exception.message ?: "잘못된 요청입니다.",
                ),
            )

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNotFoundException(exception: NoResourceFoundException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ApiErrorResponse(
                    code = "NOT_FOUND",
                    message = "요청한 리소스를 찾을 수 없습니다.",
                ),
            )

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(exception: Exception): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiErrorResponse(
                    code = "INTERNAL_SERVER_ERROR",
                    message = "처리 중 오류가 발생했습니다.",
                ),
            )

    private fun FieldError.safeMessage(): String =
        defaultMessage ?: "올바르지 않은 값입니다."
}
