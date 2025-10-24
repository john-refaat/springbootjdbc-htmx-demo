package dev.js.productsdemo.controllers

import dev.js.productsdemo.exceptions.ProductNotFoundException
import dev.js.productsdemo.exceptions.UniqueViolationException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartException

@ControllerAdvice
class GlobalExceptionHandler {

    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    /**
     * Handle product not found Exception
     */
    @ExceptionHandler(ProductNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleProductNotFound(ex: ProductNotFoundException,
                              model: Model,
                              response: HttpServletResponse): String {
        logger.error("Product not found: {}", ex.message)
        model.addAttribute("errorMessage", ex.message ?: "Product not found")
        response.addHeader("HX-Reswap", "innerHTML")
        return "fragments/error :: error-message"
    }

    /**
     * Handle database errors
     */
    @ExceptionHandler(DataAccessException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleDataAccessError(
        ex: DataAccessException,
        model: Model,
        response: HttpServletResponse): String {
        logger.error("Database error", ex)

        model.addAttribute("errorMessage", "Database error occurred. Please try again later.")
        response.addHeader("HX-Reswap", "innerHTML")
        return "fragments/error :: error-message"
    }

    /**
     * Handle unique constraint violation
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UniqueViolationException::class)
    fun handleUniqueConstraintError(
        ex: UniqueViolationException,
        model: Model,
        response: HttpServletResponse
    ): String {
        logger.error("Unique Constraint Violation: {}", ex.message)
        model.addAttribute("errorMessage", ex.message ?: "Invalid request")
        response.addHeader("HX-Reswap", "innerHTML")
        return "fragments/error :: error-message"
    }

    /**
     * Handle file upload errors
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MultipartException::class, MaxUploadSizeExceededException::class)
    fun handleFileUploadError(
        ex: Exception,
        request: HttpServletRequest,
        model: Model,
        response: HttpServletResponse
    ): String {
        logger.error("File upload error", ex)

        model.addAttribute("errorMessage", "File upload failed. File may be too large or invalid format.")
        response.addHeader("HX-Reswap", "innerHTML")
        return "fragments/error :: error-message"
    }

    /**
     *     Catch-all for any other exceptions
     *
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleGenericError(
        ex: Exception,
        model: Model,
        response: HttpServletResponse
    ): String {
        logger.error("Unexpected error", ex)

        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.")

        response.addHeader("HX-Reswap", "innerHTML")
        return "fragments/error :: error-message"
    }
}