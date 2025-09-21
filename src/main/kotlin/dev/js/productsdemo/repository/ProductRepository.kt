package dev.js.productsdemo.repository

import dev.js.productsdemo.domain.Product


interface ProductRepository {
    fun findAllProducts(): List<Product>
    fun findAllProductsWithDetails(): List<Product>
    fun findProductById(id: Long): Product?
    fun findProductByExternalId(externalId: Long): Product?
    fun findProductWithDetailsById(id: Long): Product
    fun saveProduct(product: Product): Product
    fun updateProduct(product: Product): Product?
    fun saveOrUpdateProduct(product: Product): Product?
    fun deleteProduct(id: Long): Boolean
    fun deleteAllProducts()
}