package dev.js.productsdemo.domain

import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.time.OffsetDateTime

data class Product(
    val id: Long? = null,
    val externalId: Long?,
    val title: String,
    val vendor: String?,
    val productType: String?,
    val createdAt: OffsetDateTime? = null,
    val variants: List<Variant>? = null
)


data class Variant(
    val id: Long? = null,
    val externalId: Long?,
    val productId: Long?,
    val featuredImage: Image? = null,
    val title: String,
    val option1: String?,
    val option2: String?,
    val option3: String?,
    val sku: String,
    val price: BigDecimal?,
    val available: Boolean = true,
    val createdAt: OffsetDateTime? = null,
    val imageFile: MultipartFile? = null
)

data class Image(
    val id: Long? = null,
    val externalId: Long? = null,
    val src: String?,
    val createdAt: OffsetDateTime? = null
)
