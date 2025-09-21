package dev.js.productsdemo.mappers

import dev.js.productsdemo.domain.Image
import dev.js.productsdemo.domain.Product
import dev.js.productsdemo.domain.Variant
import dev.js.productsdemo.model.ImageDTO
import dev.js.productsdemo.model.ProductDTO
import dev.js.productsdemo.model.VariantDTO

fun Image.toImageDTO(): ImageDTO {
    return ImageDTO(
        uid = this.id,
        externalId = this.externalId,
        src = this.src,
        createdAt = this.createdAt
    )
}


fun Variant.toVariantDTO(): VariantDTO {
    return VariantDTO(
        uid = this.id,
        externalId = this.externalId,
        productId = this.productId,
        title = this.title,
        option1 = this.option1,
        option2 = this.option2,
        option3 = this.option3,
        sku = this.sku,
        price = this.price,
        available = this.available,
        createdAt = this.createdAt,
        featuredImage = this.featuredImage?.toImageDTO()
    )
}

fun Product.toProductDTO(): ProductDTO {
    return ProductDTO(
        uid = this.id,
        externalId = this.externalId,
        title = this.title,
        vendor = this.vendor,
        productType = this.productType,
        createdAt = this.createdAt,
        variants = this.variants?.map { it.toVariantDTO() }
    )
}