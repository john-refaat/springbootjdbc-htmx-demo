package dev.js.productsdemo.service

import dev.js.productsdemo.domain.Product
import dev.js.productsdemo.model.ProductDTO
import dev.js.productsdemo.model.ProductsResponse

interface ProductService {
    fun getAllProductsWithDetails(): ProductsResponse
    fun getProductById(id: Long): ProductDTO
    fun saveProduct(product: Product): ProductDTO
    fun fetchAndSaveExternalProducts()
    fun getAllProducts(): ProductsResponse
}