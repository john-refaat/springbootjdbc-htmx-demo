package dev.js.productsdemo.service

import dev.js.productsdemo.domain.Image
import dev.js.productsdemo.domain.Product
import dev.js.productsdemo.domain.Variant
import dev.js.productsdemo.mappers.toProductDTO
import dev.js.productsdemo.mappers.toVariantDTO
import dev.js.productsdemo.model.ImageDTO
import dev.js.productsdemo.model.ProductDTO
import dev.js.productsdemo.model.ProductsResponse
import dev.js.productsdemo.model.VariantDTO
import dev.js.productsdemo.repository.ImageRepository
import dev.js.productsdemo.repository.ProductRepository
import dev.js.productsdemo.repository.VariantRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import kotlin.jvm.java

@Service
class ProductServiceImpl(

    private val productRepository: ProductRepository,
    private val variantRepository: VariantRepository,
    private val imageRepository: ImageRepository,
    private val restTemplate: RestTemplate = RestTemplate()
) : ProductService {
    private val logger = LoggerFactory.getLogger(ProductService::class.java)

    // Now just delegates to the optimized repository method
    override fun getAllProductsWithDetails(): ProductsResponse {
        return ProductsResponse(
            productRepository.findAllProductsWithDetails()
                .map { it.toProductDTO() }
        )
    }

    override fun saveProduct(product: Product): ProductDTO {
        return productRepository.saveProduct(product).toProductDTO()
    }

    override fun getProductById(id: Long): ProductDTO {
        return productRepository.findProductById(id)
            ?.toProductDTO()
            ?: throw RuntimeException("Product $id not found")
    }


    override fun getAllProducts(): ProductsResponse {
        val products = productRepository.findAllProducts()
        return ProductsResponse(
            products = products.map { it.toProductDTO() }
        )
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
            products.take(50).forEach { product ->
                logger.info("Saving product ${product.title} (${product.externalId})")
                println(product)
                // Save the product
                val savedProduct = productRepository.saveProduct(
                    Product(
                        externalId = product.externalId,
                        title = product.title,
                        vendor = product.vendor,
                        productType = product.productType,
                        createdAt = product.createdAt
                    )
                )

                val productId = savedProduct.id!!

                // Save variants
                product.variants?.forEach { variantDTO ->

                    val featuredImage = imageRepository.findImageByExternalId(variantDTO.featuredImage?.externalId ?: -1L)
                        ?: variantDTO.featuredImage?.let { imageDTO ->
                            imageRepository.saveImage(
                                image = Image(
                                    externalId = imageDTO.externalId,
                                    src = imageDTO.src,
                                    createdAt = imageDTO.createdAt
                                )
                            )
                        }

                    variantRepository.saveOrUpdateVariant(
                        variant = Variant(
                            externalId = variantDTO.externalId,
                            productId = productId,
                            featuredImage = featuredImage,
                            title = variantDTO.title,
                            option1 = variantDTO.option1,
                            option2 = variantDTO.option2,
                            option3 = variantDTO.option3,
                            sku = variantDTO.sku,
                            price = variantDTO.price,
                            available = variantDTO.available,
                            createdAt = variantDTO.createdAt
                        )
                    )
                }

            }

            logger.info("Successfully saved ${products.take(50).size} products with variants and images")

        } catch (e: Exception) {
            logger.error("Error fetching external products", e)
        }
    }
}