package dev.js.productsdemo.service

import dev.js.productsdemo.domain.Image
import dev.js.productsdemo.domain.Variant
import dev.js.productsdemo.mappers.toProduct
import dev.js.productsdemo.mappers.toProductDTO
import dev.js.productsdemo.mappers.toVariant
import dev.js.productsdemo.mappers.toVariantDTO
import dev.js.productsdemo.model.ProductDTO
import dev.js.productsdemo.model.ProductsResponse
import dev.js.productsdemo.repository.ImageRepository
import dev.js.productsdemo.repository.ProductRepository
import dev.js.productsdemo.repository.VariantRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import kotlin.math.ceil

@Service
class ProductServiceImpl(

    private val productRepository: ProductRepository,
    private val variantRepository: VariantRepository,
    private val imageRepository: ImageRepository,
    private val restTemplate: RestTemplate = RestTemplate()
) : ProductService {
    private val logger = LoggerFactory.getLogger(ProductService::class.java)

    // Now just delegates to the optimized repository method
    override fun getAllProductsWithDetails(page: Int, pageSize: Int?): ProductsResponse {
        val count = productRepository.countAllProducts()
        val offset = if (pageSize != null) page * pageSize else null
        return ProductsResponse(
            productRepository.findAllProductsWithDetails(pageSize, offset)
                .map { it.toProductDTO() },
            currentPage = page,
            totalPages = pageSize?.run { ceil(count.toDouble() / this).toLong() },
            pageSize = pageSize
        )
    }

    override fun saveProduct(productDTO: ProductDTO): ProductDTO {
        val productDTO = productRepository.saveProduct(productDTO.toProduct()).toProductDTO()

        val variantDTOs = productDTO.variants.map { variantDTO -> variantDTO.toVariant() }
            .map { variant ->
                val savedFeaturedImage = variant.featuredImage?.let { imageRepository.saveImage(it) }
                variantRepository.saveOrUpdateVariant(variant.copy(featuredImage = savedFeaturedImage))
            }.mapNotNull { variant ->
                variant?.toVariantDTO()
            }
        return productDTO.copy(variants = variantDTOs.toMutableList())
    }

    override fun getProductById(id: Long): ProductDTO {
        return productRepository.findProductById(id)
            ?.toProductDTO()
            ?: throw RuntimeException("Product $id not found")
    }


    override fun getAllProducts(page: Int, pageSize: Int?): ProductsResponse {
        val count = productRepository.countAllProducts()
        val offset = if (pageSize != null) page * pageSize else null
        val products = productRepository.findAllProducts(pageSize, offset)
        return ProductsResponse(
            products = products.map { it.toProductDTO() },
            currentPage = page,
            totalPages = pageSize?.run { ceil(count.toDouble() / this).toLong() },
            pageSize = pageSize
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

                    val featuredImage =
                        imageRepository.findImageByExternalId(variantDTO.featuredImage?.externalId ?: -1L)
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