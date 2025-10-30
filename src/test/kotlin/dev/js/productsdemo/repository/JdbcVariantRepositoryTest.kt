package dev.js.productsdemo.repository

import dev.js.productsdemo.domain.Product
import dev.js.productsdemo.domain.Variant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import java.math.BigDecimal
import java.time.OffsetDateTime

@JdbcTest
@Import(JdbcVariantRepository::class, JdbcProductRepository::class)
class JdbcVariantRepositoryTest {

    @Autowired
    private lateinit var variantRepository: JdbcVariantRepository

    @Autowired
    private lateinit var productRepository: JdbcProductRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private lateinit var testProduct: Product
    private lateinit var testVariant: Variant

    @BeforeEach
    fun setup() {

        testProduct = productRepository.saveProduct(
            Product(
                title = "Test Product",
                externalId = 999988887777,
                vendor = "Test Vendor",
                productType = "Test Type",
            )
        )
        requireNotNull(testProduct.id) { "Failed to get valid ID from created product" }
        testVariant = Variant(
            productId = testProduct.id,
            title = "Test Variant",
            sku = "TEST-SKU-001",
            price = BigDecimal("19.99"),
            option1 = "Red",
            option2 = "Large",
            option3 = null,
            available = true,
            externalId = 1234567890,
            createdAt = OffsetDateTime.now()
        )
    }

    @Test
    fun findById_ShouldReturnVariant() {
        val savedVariant = variantRepository.saveOrUpdateVariant(testVariant)
        val found = variantRepository.findVariantById(savedVariant?.id!!)
        assertThat(found).isNotNull
        assertThat(found?.id).isEqualTo(savedVariant.id)
    }

    @Test
    fun create_ShouldInsertNewVariant() {
        val created = variantRepository.saveOrUpdateVariant(testVariant)
        assertNotNull(created)
        assertThat(created.id).isNotNull()
        assertThat(created.title).isEqualTo(testVariant.title)
        assertThat(created.sku).isEqualTo(testVariant.sku)
    }

    @Test
    fun update_ShouldModifyExistingVariant() {
        val created = variantRepository.saveOrUpdateVariant(testVariant)
        requireNotNull(created) { "Failed to create test variant" }
        val updatedVariant = created.copy(title = "Updated Title")
        val result = variantRepository.saveOrUpdateVariant(updatedVariant)
        assertThat(result?.title).isEqualTo("Updated Title")
    }


    @Test
    fun delete_ShouldRemoveVariant() {
        val created = variantRepository.saveOrUpdateVariant(testVariant)
        val idToDelete = created?.id
        requireNotNull(idToDelete) { "Failed to get valid ID from created variant" }

        variantRepository.deleteVariant(idToDelete)
        val found = variantRepository.findVariantById(idToDelete)
        assertThat(found).isNull()
    }
}