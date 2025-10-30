package dev.js.productsdemo.repository

import dev.js.productsdemo.domain.Product
import dev.js.productsdemo.domain.Variant
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import
import java.math.BigDecimal
import java.time.OffsetDateTime

@JdbcTest
@Import(
    JdbcProductRepository::class, JdbcImageRepository::class,
    JdbcVariantRepository::class
)
class JdbcProductRepositoryTest @Autowired constructor(
    val productRepository: ProductRepository,
    val variantRepository: VariantRepository,
    val imageRepository: ImageRepository
) {

    @BeforeEach
    fun setup() {

    }

    @Test
    fun `should save product successfully`() {
        val product = Product(
            title = "Test Product",
            externalId = 999988887777,
            vendor = "Test Vendor",
            productType = "Test Type",
            createdAt = OffsetDateTime.now()
        )

        val savedProduct = productRepository.saveProduct(product)

        assertNotNull(savedProduct.id)
        assertEquals(product.title, savedProduct.title)
        assertEquals(product.externalId, savedProduct.externalId)
        assertEquals(product.vendor, savedProduct.vendor)
        assertEquals(product.productType, savedProduct.productType)
        assertNotNull(savedProduct.createdAt)
    }

    @Test
    fun `should find product by id`() {
        val product = Product(
            title = "Test Product",
            externalId = 999988887777,
            vendor = "Test Vendor",
            productType = "Test Type",
            createdAt = OffsetDateTime.now()
        )
        val savedProduct = productRepository.saveProduct(product)

        val foundProduct = productRepository.findProductById(savedProduct.id!!)

        assertNotNull(foundProduct)
        assertEquals(savedProduct.id, foundProduct?.id)

        assertEquals(product.title, foundProduct?.title)
        assertEquals(product.externalId, foundProduct?.externalId)
        assertEquals(product.vendor, foundProduct?.vendor)
        assertEquals(product.productType, foundProduct?.productType)
        assertNotNull(foundProduct?.createdAt)
    }

    @Test
    fun `should find all products`() {
        val product1 = productRepository.saveProduct(
            Product(
                title = "Product 1",
                externalId = 999988887777,
                vendor = "Test Vendor",
                productType = "Test Type",
                createdAt = OffsetDateTime.now()
            )
        )
        val product2 = productRepository.saveProduct(
            Product(
                title = "Product 2",
                externalId = 999988887778,
                vendor = "Test Vendor",
                productType = "Test Type"
            )
        )

        val products = productRepository.findAllProducts(limit = 10, offset = 0)

        assertTrue(products.size >= 2)
        assertTrue(products.any { it.id == product1.id })
        assertTrue(products.any { it.id == product2.id })
    }

    @Test
    fun `should find all products with variants`() {
        val product1 = productRepository.saveProduct(
            Product(
                title = "Product 1",
                externalId = 999988887777,
                vendor = "Test Vendor",
                productType = "Test Type",
                createdAt = OffsetDateTime.now(),
            )
        )
        val product2 = productRepository.saveProduct(
            Product(
                title = "Product 2",
                externalId = 999988887778,
                vendor = "Test Vendor",
                productType = "Test Type",
                createdAt = OffsetDateTime.now(),
            )
        )
        variantRepository.saveOrUpdateVariant(
            Variant(
                externalId = 8522558,
                title = "Test Variant",
                productId = product1.id!!,
                sku = "test-sku",
                price = BigDecimal("10.99"),
                available = true,
                createdAt = OffsetDateTime.now(),
                option1 = "test",
                option2 = null,
                option3 = null
            )
        )
        variantRepository.saveOrUpdateVariant(
            Variant(
                externalId = 8522559,
                title = "Test Variant 2",
                productId = product2.id!!,
                sku = "test-sku-2",
                price = BigDecimal("10.99"),
                available = true,
                createdAt = OffsetDateTime.now(),
                option1 = "test",
                option2 = null,
                option3 = null
            )
        )

        val products = productRepository.findAllProductsWithDetails(limit = 10, offset = 0)
            .sortedBy { it.id }
        assertTrue(products.size >= 2)
        assertTrue(products.any { it.id == product1.id })
        assertTrue(products.any { it.id == product2.id })
        assertEquals(1, products.find { it.id == product1.id }?.variants?.size)
        assertEquals(1, products.find { it.id == product2.id }?.variants?.size)
        assertEquals("Test Variant", products.find { it.id == product1.id }?.variants?.get(0)?.title)
        assertEquals("Test Variant 2", products.find { it.id == product2.id }?.variants?.get(0)?.title)
    }

    @Test
    fun `should update product`() {
        val product = productRepository.saveProduct(
            Product(
                id = 56789,
                title = "Test Product",
                externalId = 999988887777,
                vendor = "Test Vendor",
                productType = "Test Type",
                createdAt = OffsetDateTime.now()
            )
        )


        val updatedProduct = product.copy(
            title = "Updated Name",
            externalId = 999988887778
        )

        productRepository.updateProduct(updatedProduct)

        val foundProduct = productRepository.findProductById(product.id!!)

        assertNotNull(foundProduct)
        assertEquals(updatedProduct.id, foundProduct?.id)
    }

    @Test
    fun `should delete product`() {
        val product = productRepository.saveProduct(
            Product(
                title = "Test Product",
                externalId = 999988887777,
                vendor = "Test Vendor",
                productType = "Test Type",
                createdAt = OffsetDateTime.now()
            )
        )

        productRepository.deleteProduct(product.id!!)

        val foundProduct = productRepository.findProductById(product.id!!)
        assertNull(foundProduct)
    }
}