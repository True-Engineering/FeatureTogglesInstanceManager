package ru.trueengineering.featureflag.manager.ports.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import ru.trueengineering.featureflag.manager.core.error.ErrorCode
import ru.trueengineering.featureflag.manager.core.error.ServiceException

@ControllerAdvice
class RestExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ServiceException::class)
    fun handleServiceException(ex: ServiceException, request: WebRequest?): ResponseEntity<ErrorResponse?>? {
        val errorResponse = ErrorResponse(ex.errorMessage ?: "", ex.errorCode)

        val status = when (ex.errorCode) {
            ErrorCode.ENVIRONMENT_NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorCode.PROJECT_NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorCode.ORGANIZATION_NOT_FOUND -> HttpStatus.NOT_FOUND
            ErrorCode.ACCESS_DENIED -> HttpStatus.FORBIDDEN
            else -> HttpStatus.BAD_REQUEST
        }
        return ResponseEntity(errorResponse, status)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException,
                                              request: WebRequest?): ResponseEntity<ErrorResponse?>? {
        log.error("Error: message [{}]", ex.message, ex)
        val errorResponse = ErrorResponse(ex.message ?: "Invalid input data", ErrorCode.INVALID_INPUT_DATA)
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handleAccessDeniedException(ex: org.springframework.security.access.AccessDeniedException,
                                              request: WebRequest?): ResponseEntity<ErrorResponse?>? {
        log.error("Error: message [{}]", ex.message, ex)
        val errorResponse = ErrorResponse(ex.message ?: "Access denied", ErrorCode.ACCESS_DENIED)
        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception, request: WebRequest?): ResponseEntity<ErrorResponse?>? {
        log.error("Error: message [{}]", ex.localizedMessage, ex)
        val errorResponse = ErrorResponse(ex.localizedMessage ?: "Unknown error", ErrorCode.INTERNAL_ERROR)
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    data class ErrorResponse(val errorMessage: String, val errorCode: ErrorCode)
}