package dev.js.productsdemo.controllers

import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler {

    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    
    @ExceptionHandler(ProductNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleProductNotFound(ex: ProductNotFoundException,
                              model: Model): String {
        logger.error("Product not found: {}", ex.message)
        model.addAttribute("errorMessage", ex.message ?: "Product not found")
        return "fragments/error :: error-message"
    }

    @ExceptionHandler(DataAccessException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleDataAccessError(
        ex: DataAccessException,
        model: Model): String {
        logger.error("Database error", ex)

        model.addAttribute("errorMessage", "Database error occurred. Please try again later.")

        return "fragments/error :: error-message"
    }
}