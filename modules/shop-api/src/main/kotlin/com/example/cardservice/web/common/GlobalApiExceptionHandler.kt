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
                    result = ApplicationResponseType.VALIDATION_ERROR.toResult(),
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
                    result = ApplicationResponseType.BAD_REQUEST.toResult(
                        exception.message ?: ApplicationResponseType.BAD_REQUEST.message,
                    ),
                ),
            )

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNotFoundException(exception: NoResourceFoundException): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ApiErrorResponse(
                    result = ApplicationResponseType.NOT_FOUND.toResult(),
                ),
            )

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(exception: Exception): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiErrorResponse(
                    result = ApplicationResponseType.INTERNAL_SERVER_ERROR.toResult(),
                ),
            )

    private fun FieldError.safeMessage(): String =
        defaultMessage ?: "올바르지 않은 값입니다."
}
