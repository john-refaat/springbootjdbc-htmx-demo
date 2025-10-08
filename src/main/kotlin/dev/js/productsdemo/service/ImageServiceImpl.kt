package dev.js.productsdemo.service

import dev.js.productsdemo.domain.Image
import dev.js.productsdemo.mappers.toImage
import dev.js.productsdemo.mappers.toImageDTO
import dev.js.productsdemo.model.ImageDTO
import dev.js.productsdemo.model.VariantDTO
import dev.js.productsdemo.repository.ImageRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime

@Service
class ImageServiceImpl(
    @Value("\${app.upload.dir}")
    private val uploadDir: String,
    private val imageRepository: ImageRepository
): ImageService {
    private val logger = LoggerFactory.getLogger(ImageServiceImpl::class.java)

    companion object {
        private const val ERROR_NULL_IMAGE = "Image file cannot be null"
    }

    override fun storeImageFile(
        variantDTO: VariantDTO
    ): String {
        logger.info("Storing image file for product: ${variantDTO.productId}, variant: ${variantDTO.title}")
        if (variantDTO.imageFile == null) {
            logger.error("Image file is null for variant: ${variantDTO.title}")
            throw IllegalArgumentException("Image file cannot be null")
        }
        val filename = generateUniqueFilename(variantDTO.title, variantDTO.imageFile.originalFilename)

        val directory = File("$uploadDir/${variantDTO.productId}")
        if (!directory.exists()) {
            logger.debug("Creating directory: ${directory.absolutePath}")
            directory.mkdirs()
        }

        val destinationFile = File(directory, filename)

        val normalizedPath = destinationFile.canonicalPath
        if (!normalizedPath.startsWith(directory.canonicalPath)) {
            logger.error("Security violation: attempted path traversal attack")
            throw SecurityException("Invalid file path")
        }

        variantDTO.imageFile.inputStream.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        logger.info("Successfully stored image file: ${variantDTO.productId}/$filename")
        return "images/${variantDTO.productId}/$filename"
    }

    override fun saveImage(
        variantDTO: VariantDTO
    ): ImageDTO {
        logger.info("Saving image for variant: ${variantDTO.title}")

        val savedImage = createAndSaveImage(variantDTO)

        logger.info("Successfully saved image with ID: ${savedImage.id}")
        return savedImage.toImageDTO()
    }
    private fun createAndSaveImage(variantDTO: VariantDTO): Image {
        return if (variantDTO.featuredImage != null) {
            // Use existing image data if available
            imageRepository.saveImage(variantDTO.featuredImage.toImage())
        } else {
            // Generate and store a new image
            val imagePath = storeImageFile(variantDTO)
            imageRepository.saveImage(Image(
                src = imagePath,
                createdAt = OffsetDateTime.now()
            ))
        }
    }
    override fun findImageById(id: Long): ImageDTO? {
        logger.info("Fetching image with ID: $id")
        return imageRepository.findImageById(id)?.toImageDTO()
    }

    override fun findImageByExternalId(externalId: Long): ImageDTO? {
        logger.info("Fetching image with external ID: $externalId")
        return imageRepository.findImageByExternalId(externalId)?.toImageDTO()
    }

    override fun deleteImage(id: Long) {
        logger.info("Deleting image with ID: $id")
        imageRepository.deleteImage(id)
        logger.info("Successfully deleted image with ID: $id")
    }

    private fun generateUniqueFilename(title: String, originalFileName: String?=".jpg"): String {
        val timestamp = Instant.now().toEpochMilli()
        val extension = originalFileName?.substringAfterLast(".")
        return "${title}_${timestamp}.$extension"
    }
}