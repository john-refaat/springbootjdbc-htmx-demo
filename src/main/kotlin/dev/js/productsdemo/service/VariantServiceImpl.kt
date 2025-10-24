package dev.js.productsdemo.service

import dev.js.productsdemo.exceptions.UniqueViolationException
import dev.js.productsdemo.mappers.toVariant
import dev.js.productsdemo.mappers.toVariantDTO
import dev.js.productsdemo.model.VariantDTO
import dev.js.productsdemo.repository.VariantRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class VariantServiceImpl(
    val variantRepository: VariantRepository,
    val imageService: ImageService
) : VariantService {

    private val logger = LoggerFactory.getLogger(VariantServiceImpl::class.java)

    override fun saveVariants(variants: List<VariantDTO>): List<VariantDTO> {
        val duplicateExternalId = variants.groupBy { it.externalId }.any { (_, variants) -> variants.size > 1 }
        if (duplicateExternalId)
            throw UniqueViolationException("External ID must be unique for each variant")
        val duplicateTitles = variants.groupBy { it.title }.any { (_, variants) -> variants.size > 1 }
        if (duplicateTitles)
            throw UniqueViolationException("Title must be unique for each variant")


        return variants.mapNotNull {
            saveVariant(it)
        }
    }

    override fun saveVariant(variant: VariantDTO): VariantDTO? {
        logger.info("Saving image for variant (${variant.title})")
        variant.externalId?.apply {
            if (variantRepository.externalIdExists(variant.externalId))
                throw UniqueViolationException("External ID ${variant.externalId} already exists for a variant")
        }

        val featuredImage = if (variant.featuredImage != null && variant.featuredImage.externalId != null) {
            imageService.findImageByExternalId(variant.featuredImage.externalId)
        } else if (variant.featuredImage != null || variant.imageFile != null) {
            imageService.saveImage(variant)
        } else null

        return variantRepository.saveOrUpdateVariant(variant.copy(featuredImage = featuredImage).toVariant())
            ?.toVariantDTO()
    }
}