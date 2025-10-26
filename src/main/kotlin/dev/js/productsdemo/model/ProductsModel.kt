package dev.js.productsdemo.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length
import jakarta.validation.constraints.NotNull
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

    @field:NotNull(message = "External id cannot be blank")
    @field:Min(1)
    @param:JsonProperty("id")
    val externalId: Long? = null,

    @field:NotBlank(message = "Title cannot be blank")
    @field:Length(min = 3, max = 50)
    val title: String = "",

    @field:NotBlank(message = "Vendor cannot be blank")
    @field:Length(min = 3, max = 50, message = "Vendor must be between 3 and 50 characters")
    val vendor: String = "",

    @field:NotBlank(message = "Product type cannot be blank")
    @field:Length(min = 3, max = 50, message = "Product type must be between 3 and 50 characters")
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

    @field:NotNull(message = "External id cannot be blank")
    @field:Min(1)
    @param:JsonProperty("id")
    val externalId: Long? = null,

    @param:JsonProperty("product_id")
    val productId: Long? = null,

    @field:NotBlank(message = "Title cannot be blank")
    @field:Length(min = 3, max = 50, message = "Title must be between 3 and 50 characters")
    val title: String = "",

    val option1: String? = "",
    val option2: String? = "",
    val option3: String? = "",

    @field:NotBlank(message = "SKU cannot be blank")
    @field:Length(min = 3, max = 50, message = "SKU must be between 3 and 50 characters")
    val sku: String = "",

    @field:NotNull(message = "Price cannot be empty")
    @field:DecimalMin("0.01", message = "Price must be greater than or equal to 0.01")
    @field:DecimalMax("9999.99", message = "Price must be less than or equal to 9999.99")
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
