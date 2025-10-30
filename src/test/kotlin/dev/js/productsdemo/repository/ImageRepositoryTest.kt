package dev.js.productsdemo.repository

import dev.js.productsdemo.domain.Image
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import

@JdbcTest
@Import(JdbcImageRepository::class)
class ImageRepositoryTest @Autowired constructor(
    val imageRepository: ImageRepository){

    private lateinit var savedImage: Image

    @BeforeEach
    fun setup() {
        val count = imageRepository.findAllImages().count()
        println("Images found: $count")
        savedImage = imageRepository.saveImage(
            Image(
                externalId = 1L,
                src = "test.png",
            )
        )
    }

    @Test
    fun `should save an image`() {
        val image = imageRepository.saveImage(
            Image(
                externalId = 2L,
                src = "test2.png",
            )
        )
        assert(image.id != null)
        print(image)
    }

    @Test
    fun `should find image by id`() {
        val foundImage = imageRepository.findImageById(savedImage.id!!)
        assert(foundImage != null)
        assert(foundImage?.src == "test.png")
    }

    @Test
    fun `should return null when finding non-existent image`() {
        val nonExistentImage = imageRepository.findImageById(-1)
        assert(nonExistentImage == null)
    }

    @Test
    fun `should delete image`() {
        imageRepository.deleteImage(savedImage.id!!)
        val deletedImage = imageRepository.findImageById(savedImage.id!!)
        assert(deletedImage == null)
    }

    

}