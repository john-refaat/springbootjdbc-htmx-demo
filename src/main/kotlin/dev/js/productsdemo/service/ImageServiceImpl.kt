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

        validateOriginalFilename(variantDTO.imageFile.originalFilename)


        val filename = generateUniqueFilename(
            variantDTO.title.replace(Regex("[\\s/\\\\<>:\"|?*]"), "_"),
            variantDTO.imageFile.originalFilename
        )

        val dirRelativePath = "images/${variantDTO.productId}"
        val directory = File(uploadDir, dirRelativePath)
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
        return "$dirRelativePath/$filename"
    }

    override fun saveImage(
        variantDTO: VariantDTO
    ): ImageDTO? {
        logger.info("Saving image for variant: ${variantDTO.title}")
        if (variantDTO.featuredImage == null && variantDTO.imageFile == null) {
            logger.error("Tried to Save Image but no Image attached to variant")
            return null
        }

        val savedImage = createAndSaveImage(variantDTO)

        logger.info("Successfully saved image with ID: ${savedImage.id}")
        return savedImage.toImageDTO()
    }
    private fun createAndSaveImage(variantDTO: VariantDTO): Image {
        return if (variantDTO.featuredImage != null) {
            // Use existing image data if available
            imageRepository.saveImage(variantDTO.featuredImage.toImage())
        } else if (variantDTO.imageFile != null) {
            // Generate and store a new image
            val imagePath = storeImageFile(variantDTO)
            imageRepository.saveImage(Image(
                src = imagePath,
                createdAt = OffsetDateTime.now()
            ))
        } else {
            throw IllegalArgumentException("Tried to save image, but no image found.")
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
        val extension = originalFileName?.substringAfterLast(".")?.takeIf { it.isNotBlank() } ?: "jpg"
        return "${title}_${timestamp}.$extension"
    }

    private fun validateOriginalFilename(filename: String?) {
        if (filename.isNullOrBlank()) {
            logger.error("Original filename is null or blank")
            throw IllegalArgumentException("Original filename cannot be null or blank")
        }

        if (!filename.matches(Regex("^[a-zA-Z0-9._-]+$"))) {
            logger.error("Invalid original filename: $filename")
            throw IllegalArgumentException("Original filename contains invalid characters")
        }
    }

}