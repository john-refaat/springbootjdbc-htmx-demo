package dev.js.productsdemo.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length
import org.jetbrains.annotations.NotNull
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.time.OffsetDateTime


data class ProductRequest(
    @field:Valid
    val product: ProductDTO,
    var pageSize: Int = 5,
    val mode:String = "create"
)

data class ProductsResponse(
    val products: List<ProductDTO>,
    val currentPage: Int = 0,
    val totalPages: Long?,
    val pageSize: Int?
)


data class ProductDTO(
    val uid: Long? = null,

    @field:Min(1)
    @param:JsonProperty("id")
    val externalId: Long? = null,

    @field:NotBlank
    @field:Length(min = 3, max = 50)
    val title: String = "",

    @field:NotBlank
    @field:Length(min = 3, max = 50)
    val vendor: String = "",

    @field:NotBlank
    @field:Length(min = 3, max = 50)
    @param:JsonProperty("product_type")
    val productType: String = "",

    @param:JsonProperty("created_at")
    val createdAt: OffsetDateTime? = null,

    @field:Valid
    @param:JsonProperty("variants")
    var variants: List<VariantDTO> = listOf(),

) {
    // Computed property - calculated each time it's accessed
    val displayImage: ImageDTO?
        get() = variants.firstOrNull { it.featuredImage != null }?.featuredImage
}

data class VariantDTO(
    val uid: Long? = null,

    @field:Min(1)
    @param:JsonProperty("id")
    val externalId: Long? = null,
    @param:JsonProperty("product_id")
    val productId: Long? = null,
    @field:NotBlank
    @field:Length(min = 3, max = 50)
    val title: String = "",
    val option1: String? = "",
    val option2: String? = "",
    val option3: String? = "",
    @field:Length(min = 3, max = 50)
    val sku: String = "",

    @field:NotNull
    @field:DecimalMin("1.00")
    @field:DecimalMax("1000.00")
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
