package dev.js.productsdemo.service

import dev.js.productsdemo.domain.Variant
import dev.js.productsdemo.model.ImageDTO
import dev.js.productsdemo.model.VariantDTO
import org.springframework.web.multipart.MultipartFile

interface ImageService {

    fun storeImageFile(variantDTO: VariantDTO): String
    fun saveImage(variantDTO: VariantDTO): ImageDTO?
    fun findImageById(id: Long): ImageDTO?
    fun findImageByExternalId(externalId: Long): ImageDTO?
    fun deleteImage(id: Long)
}