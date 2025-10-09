package dev.js.productsdemo.service

import dev.js.productsdemo.domain.Image
import dev.js.productsdemo.domain.Variant
import dev.js.productsdemo.mappers.toProduct
import dev.js.productsdemo.mappers.toProductDTO
import dev.js.productsdemo.mappers.toVariant
import dev.js.productsdemo.mappers.toVariantDTO
import dev.js.productsdemo.model.ProductDTO
import dev.js.productsdemo.model.ProductsResponse
import dev.js.productsdemo.model.VariantDTO
import dev.js.productsdemo.repository.ImageRepository
import dev.js.productsdemo.repository.ProductRepository
import dev.js.productsdemo.repository.VariantRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import kotlin.math.ceil

@Service
class ProductServiceImpl(

    private val imageService: ImageService,
    private val productRepository: ProductRepository,
    private val variantRepository: VariantRepository,
    private val imageRepository: ImageRepository,
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
        val productDTO = productRepository.saveProduct(productDTO.toProduct()).toProductDTO()
        logger.info("Product saved with ID: ${productDTO.uid}")

        val savedVariants = mutableListOf<VariantDTO>()
        productDTO.variants.filter {
            // This will handle cases where variants were removed in the UI
                variant ->
            !variant.title.isBlank() || variant.imageFile != null
        }.forEach { variantDTO ->
            logger.info("Saving image for variant (${variantDTO.title})")
            val savedImage = if (variantDTO.featuredImage != null || variantDTO.imageFile != null)
                imageService.saveImage(variantDTO)
            else null

            variantRepository.saveOrUpdateVariant(variantDTO.copy(featuredImage = savedImage).toVariant())
                ?.apply {
                    savedVariants.add(this.toVariantDTO())
                }

        }
        return productDTO.copy(variants = savedVariants)
    }

    override fun getProductById(id: Long): ProductDTO {
        logger.info("Fetching product with ID: $id")
        return productRepository.findProductById(id)
            ?.toProductDTO()
            ?.also { logger.info("Successfully retrieved product: ${it.title}") }
            ?: throw RuntimeException("Product $id not found").also { logger.error("Product $id not found") }
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