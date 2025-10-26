package dev.js.productsdemo.exceptions

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
class VariantNotFoundException(message: String) : RuntimeException(message)