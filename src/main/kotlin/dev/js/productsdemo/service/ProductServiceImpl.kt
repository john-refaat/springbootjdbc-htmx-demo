package dev.js.productsdemo.service

import dev.js.productsdemo.controllers.ProductNotFoundException
import dev.js.productsdemo.controllers.UniqueViolationException
import dev.js.productsdemo.mappers.toProduct
import dev.js.productsdemo.mappers.toProductDTO
import dev.js.productsdemo.model.ProductDTO
import dev.js.productsdemo.model.ProductsResponse
import dev.js.productsdemo.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import kotlin.math.ceil

@Service
class ProductServiceImpl(

    private val productRepository: ProductRepository,
    private val variantService: VariantService,
    private val restTemplate: RestTemplate = RestTemplate()

) : ProductService {
    private val logger = LoggerFactory.getLogger(ProductService::class.java)

    // Now just delegates to the optimized repository method
    override fun getAllProductsWithDetails(page: Int, pageSize: Int?): ProductsResponse {
        logger.info("Fetching all products with details for page $page with size $pageSize")
        val count = productRepository.countAllProducts()
        val offset = if (pageSize != null) page * pageSize else null
        val response = ProductsResponse(
            productRepository.findAllProductsWithDetails(pageSize, offset)
                .map { it.toProductDTO() },
            currentPage = page,
            totalPages = pageSize?.run { ceil(count.toDouble() / this).toLong() },
            pageSize = pageSize
        )
        logger.info("Successfully retrieved ${response.products.size} products")
        return response
    }

    @Transactional
    override fun saveProduct(productDTO: ProductDTO): ProductDTO {
        logger.info("Saving product: ${productDTO.title}")

        productDTO.externalId?.run {
            if (productRepository.findProductByExternalId(this) != null) {
                throw UniqueViolationException("External ID $this already exists for a product")
            }
        }

        val productDTO = productRepository.saveProduct(productDTO.toProduct()).toProductDTO()
        logger.info("Product saved with ID: ${productDTO.uid}")

        val filteredVariants = productDTO.variants.filterNot {
            // This will handle cases where variants were removed in the UI
                variant ->
            variant.title.isBlank()
        }
        val savedVariants = variantService.saveVariants(filteredVariants)

        return productDTO.copy(variants = savedVariants)
    }

    @Transactional
    override fun updateProduct(uid: Long, productDTO: ProductDTO): ProductDTO {
        logger.info("Updating product: ${productDTO.title}")
        return getProductById(uid).copy(
            title = productDTO.title,
            externalId = productDTO.externalId,
            productType = productDTO.productType,
            vendor = productDTO.vendor
        )
            .run {
                productRepository.updateProduct(this.toProduct())?.toProductDTO()
            }
            ?.also { logger.info("Successfully updated product: ${it.title}") }
            ?: throw RuntimeException("Product ${productDTO.uid} not found")
                .also { logger.error("Could not update product ${productDTO.uid}. Not found") }

    }

    override fun getProductById(id: Long): ProductDTO {
        logger.info("Fetching product with ID: $id")
        return productRepository.findProductById(id)
            ?.toProductDTO()
            ?.also { logger.info("Successfully retrieved product: ${it.title}") }
            ?: throw RuntimeException("Product $id not found")
                .also { logger.error("Product $id not found") }
    }


    override fun getAllProducts(page: Int, pageSize: Int?): ProductsResponse {
        logger.info("Fetching all products for page $page with size $pageSize")
        val count = productRepository.countAllProducts()
        val offset = if (pageSize != null) page * pageSize else null
        val products = productRepository.findAllProducts(pageSize, offset)
        logger.info("Found ${products.size} products")
        return ProductsResponse(
            products = products.map { it.toProductDTO() },
            currentPage = page,
            totalPages = pageSize?.run { ceil(count.toDouble() / this).toLong() },
            pageSize = pageSize
        )
    }

    override fun deleteProduct(uid: Long) {
        logger.info("Deleting product with ID: $uid")
        val deleted = productRepository.deleteProduct(uid)
        if (deleted) {
            logger.info("Successfully deleted product with ID: $uid")
        } else {
            logger.error("Product with ID: $uid not found")
            throw ProductNotFoundException("Product $uid not found")
        }
    }

    override fun fetchAndSaveExternalProducts() {
        try {
            logger.info("Fetching products from external API...")

            val response = restTemplate.getForObject(
                "https://famme.no/products.json",
                ProductsResponse::class.java
            )

            val products = response?.products ?: emptyList()

            // Clear existing data
            productRepository.deleteAllProducts()

            // Process limited number of products
            products.take(50).forEach { productDTO ->
                logger.info("Saving product ${productDTO.title} (${productDTO.externalId})")
                println(productDTO)
                // Save the product
                val savedProduct = productRepository.saveProduct(
                    productDTO.toProduct()
                )

                val productId = savedProduct.id!!

                // Save variants
                productDTO.variants.forEach { variantDTO ->
                    variantService.saveVariant(variantDTO.copy(productId = productId))
                }

            }

            logger.info("Successfully saved ${products.take(50).size} products with variants and images")

        } catch (e: Exception) {
            logger.error("Error fetching external products", e)
        }
    }
}
