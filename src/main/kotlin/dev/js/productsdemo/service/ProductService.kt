package dev.js.productsdemo.service

import dev.js.productsdemo.domain.Product
import dev.js.productsdemo.model.ProductDTO
import dev.js.productsdemo.model.ProductsResponse
import org.springframework.web.multipart.MultipartFile
import java.rmi.server.UID

interface ProductService {
    fun getAllProductsWithDetails(page: Int, pageSize: Int?): ProductsResponse
    fun getProductById(id: Long): ProductDTO
    fun saveProduct(productDTO: ProductDTO): ProductDTO
    fun updateProduct(uid: Long, productDTO: ProductDTO): ProductDTO
    fun fetchAndSaveExternalProducts()
    fun getAllProducts(page: Int, pageSize: Int?): ProductsResponse
    fun deleteProduct(uid: Long)
}