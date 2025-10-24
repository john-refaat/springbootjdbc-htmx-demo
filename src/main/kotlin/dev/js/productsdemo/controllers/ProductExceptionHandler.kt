package dev.js.productsdemo.controllers

import dev.js.productsdemo.model.ProductRequest
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartException

@ControllerAdvice
class ProductExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(UniqueViolationException::class)
    fun handleUniqueConstraintError(
        ex: UniqueViolationException,
        request: HttpServletRequest,
        model: Model,
        response: HttpServletResponse
    ): String {
        logger.error("Unique Constraint Violation: {}", ex.message)
        response.status = HttpServletResponse.SC_BAD_REQUEST
        model.addAttribute("errorMessage", ex.message ?: "Invalid request")
        populateProductModel(request, model)
        return "fragments/form :: product-form"
    }

    private fun populateProductModel(request: HttpServletRequest, model: Model) {
        // Try to get the existing newProduct from the request attributes
        val existingProduct = request.getAttribute("newProduct") as? ProductRequest
        val visibleVariants = request.getAttribute("visibleVariants")
        if (existingProduct != null) {
            // Use the existing product data
            model.addAttribute("visibleVariants", visibleVariants)
            model.addAttribute("newProduct", existingProduct)
        } else {
            // Fallback to a new product
            model.addAttribute("visibleVariants", listOf(true, false, false))
            model.addAttribute("newProduct", ProductController.getCreateProductForm())
        }
    }


    // Handle IllegalArgumentException (from require() or check())
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException,
        request: HttpServletRequest,
        model: Model,
        response: HttpServletResponse
    ): String {
        logger.error("Illegal argument: {}", ex.message)

        response.status = HttpServletResponse.SC_BAD_REQUEST
        model.addAttribute("errorMessage", ex.message ?: "Invalid request")
        populateProductModel(request, model)
        return "fragments/form :: product-form"
    }

    // Handle file upload errors
    @ExceptionHandler(MultipartException::class, MaxUploadSizeExceededException::class)
    fun handleFileUploadError(
        ex: Exception,
        request: HttpServletRequest,
        model: Model,
        response: HttpServletResponse
    ): String {
        logger.error("File upload error", ex)

        response.status = HttpServletResponse.SC_BAD_REQUEST
        model.addAttribute("errorMessage", "File upload failed. File may be too large or invalid format.")

        populateProductModel(request, model)
        return "fragments/form :: product-form"
    }


    @ExceptionHandler(PSQLException::class)
    fun handlePSQLException(
        ex: PSQLException,
        request: HttpServletRequest,
        model: Model,
        response: HttpServletResponse
    ): String {
        logger.error("PostgreSQL error: ${ex.sqlState}, ${ex.message}", ex)

        response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        model.addAttribute("errorMessage", "Database operation failed: ${ex.message}")

        populateProductModel(request, model)
        return "fragments/form :: product-form"
    }



    // Catch-all for any other exceptions
    @ExceptionHandler(Exception::class)
    fun handleGenericError(
        ex: Exception,
        request: HttpServletRequest,
        model: Model,
        response: HttpServletResponse
    ): String {
        logger.error("Unexpected error", ex)

        response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.")

       populateProductModel(request, model)
        return "fragments/form :: product-form"
    }

}

// Custom business exceptions
sealed class ProductException(message: String) : RuntimeException(message)

class ProductNotFoundException(message: String) : ProductException(message)
class InvalidProductException(message: String) : ProductException(message)
class DuplicateProductException(message: String) : ProductException(message)
class UniqueViolationException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}