package dev.js.productsdemo.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.Instant
import java.time.OffsetDateTime


data class ProductRequest(
    val product: ProductDTO,
    val pageSize: Int = 5
)

data class ProductsResponse(
    val products: List<ProductDTO>,
    val currentPage: Int = 0,
    val totalPages: Long?,
    val pageSize: Int?
)

data class ProductDTO(
    val uid: Long? = null,
    @param:JsonProperty("id")
    val externalId: Long = Instant.now().toEpochMilli(),
    val title: String = "",
    val vendor: String = "",
    @param:JsonProperty("product_type")
    val productType: String = "",
    @param:JsonProperty("created_at")
    val createdAt: OffsetDateTime? = null,
    @param:JsonProperty("variants")
    val variants: MutableList<VariantDTO> = mutableListOf()
)

data class VariantDTO(
    val uid: Long?,
    @param:JsonProperty("id")
    val externalId: Long?,
    @param:JsonProperty("product_id")
    val productId: Long?,
    val title: String,
    val option1: String?,
    val option2: String?,
    val option3: String?,
    val sku: String,
    val price: BigDecimal?,
    val available: Boolean = true,
    @param:JsonProperty("featured_image")
    val featuredImage: ImageDTO?,
    @param:JsonProperty("created_at")
    val createdAt: OffsetDateTime?
)

data class ImageDTO(
    val uid: Long?,
    @param:JsonProperty("id")
    val externalId: Long,
    val src: String,
    @param:JsonProperty("created_at")
    val createdAt: OffsetDateTime?
)
