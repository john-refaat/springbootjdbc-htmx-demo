package dev.js.productsdemo.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.multipart.MultipartFile
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
    val uid: Long? = null,
    @param:JsonProperty("id")
    val externalId: Long? = null,
    @param:JsonProperty("product_id")
    val productId: Long? = null,
    val title: String = "",
    val option1: String? = null,
    val option2: String? = null,
    val option3: String? = null,
    val sku: String = "",
    val price: BigDecimal? = null,
    val available: Boolean = true,
    @param:JsonProperty("featured_image")
    val featuredImage: ImageDTO? = null,
    @param:JsonProperty("created_at")
    val createdAt: OffsetDateTime? = null,
    val imageFile: MultipartFile? = null
)

data class ImageDTO(
    val uid: Long? = null,
    @param:JsonProperty("id")
    val externalId: Long? = null,
    val src: String? = null,
    @param:JsonProperty("created_at")
    val createdAt: OffsetDateTime? = null,
    val imageFile: MultipartFile? = null

)
