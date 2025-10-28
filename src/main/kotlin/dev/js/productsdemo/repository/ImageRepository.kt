package dev.js.productsdemo.repository

import dev.js.productsdemo.domain.Image

interface ImageRepository {
    fun saveImage(image: Image): Image
    fun findAllImages(): List<Image>
    fun findImageById(id: Long): Image?
    fun findImageByExternalId(externalId: Long): Image?
    fun findImagesByProductId(productId: Long): List<Image>
    fun findImageByVariantId(variantId: Long): Image?
    fun deleteImage(id: Long)

}