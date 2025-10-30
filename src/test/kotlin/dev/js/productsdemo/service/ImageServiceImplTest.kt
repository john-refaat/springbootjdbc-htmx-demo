package dev.js.productsdemo.service

import dev.js.productsdemo.domain.Image
import dev.js.productsdemo.mappers.toImage
import dev.js.productsdemo.model.ImageDTO
import dev.js.productsdemo.model.VariantDTO
import dev.js.productsdemo.repository.ImageRepository
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Path
import java.time.OffsetDateTime

class ImageServiceImplTest {

    private lateinit var imageRepository: ImageRepository
    private lateinit var imageService: ImageServiceImpl
    private lateinit var uploadDir: String

    @TempDir
    lateinit var tempDir: Path


    @BeforeEach
    fun setUp() {
        imageRepository = mockk()
        uploadDir = tempDir.toString()
        imageService = ImageServiceImpl(uploadDir, imageRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    // storeImageFile tests
    @Test
    fun `storeImageFile should throw IllegalArgumentException when imageFile is null`() {
        val variantDTO = VariantDTO(
            productId = 1L,
            title = "Test Variant",
            imageFile = null
        )

        val exception = assertThrows<IllegalArgumentException> {
            imageService.storeImageFile(variantDTO)
        }

        assertEquals("Image file cannot be null", exception.message)
    }

    @Test
    fun `storeImageFile should store file successfully and return correct path`() {
        val mockFile = mockk<MultipartFile>()
        val fileContent = "test image content".toByteArray()

        every { mockFile.originalFilename } returns "test.jpg"
        every { mockFile.inputStream } returns ByteArrayInputStream(fileContent)

        val variantDTO = VariantDTO(
            productId = 123L,
            title = "Test Product",
            imageFile = mockFile
        )

        val result = imageService.storeImageFile(variantDTO)

        val expectedDirRelativeDirectory = "images/123"
        val expectedPrefix = "$expectedDirRelativeDirectory/Test_Product_"
        assertTrue(result.startsWith(expectedPrefix))
        assertTrue(result.endsWith(".jpg"))

        val savedFile = File(uploadDir, expectedDirRelativeDirectory).listFiles()?.first()
        assertNotNull(savedFile)
        assertEquals(fileContent.toString(Charsets.UTF_8), savedFile?.readText())
    }

    @Test
    fun `storeImageFile should sanitize filename with special characters`() {
        val mockFile = mockk<MultipartFile>()

        every { mockFile.originalFilename } returns "image.png"
        every { mockFile.inputStream } returns ByteArrayInputStream("data".toByteArray())

        val variantDTO = VariantDTO(
            productId = 1L,
            title = "Test/Product\\With:Special*Chars",
            imageFile = mockFile
        )

        val result = imageService.storeImageFile(variantDTO)

        assertTrue(result.contains("Test_Product_With_Special_Chars"))
        assertTrue(result.endsWith(".png"))
    }

    @Test
    fun `storeImageFile should create directory if it doesn't exist`() {
        val mockFile = mockk<MultipartFile>()

        every { mockFile.originalFilename } returns "test.jpg"
        every { mockFile.inputStream } returns ByteArrayInputStream("data".toByteArray())

        val variantDTO = VariantDTO(
            productId = 999L,
            title = "New Product",
            imageFile = mockFile
        )

        val relativeDirectory = "images/999"

        assert(!File(uploadDir, relativeDirectory).exists())

        imageService.storeImageFile(variantDTO)

        val directory = File(uploadDir, relativeDirectory)
        assertTrue(directory.exists())
        assertTrue(directory.isDirectory)
        assertEquals(1, directory.listFiles()?.size)
    }

    @Test
    fun `storeImageFile should prevent path traversal attack`() {
        val mockFile = mockk<MultipartFile>()

        every { mockFile.originalFilename } returns "malicious.jpg"
        every { mockFile.inputStream } returns ByteArrayInputStream("test data".toByteArray())

        val variantDTO = VariantDTO(
            productId = 1L,
            title = "../../MaliciousTitle",
            imageFile = mockFile
        )

        // Special Characters except dots are removed from the filename
        val expectedPrefix = "images/1/.._.._MaliciousTitle"
        val result = imageService.storeImageFile(variantDTO)
        assertTrue(result.startsWith(expectedPrefix))
        assertTrue(result.endsWith(".jpg"))

    }


    @Test
    fun `saveImage should save existing featuredImage when provided`() {
        val imageDTO = ImageDTO(
            uid = 1L,
            src = "images/test.jpg",
            externalId = 100L,
            createdAt = OffsetDateTime.now()
        )

        val savedImage = imageDTO.toImage()

        every { imageRepository.saveImage(any()) } returns savedImage

        val variantDTO = VariantDTO(
            productId = 1L,
            title = "Test",
            featuredImage = imageDTO
        )

        val result = imageService.saveImage(variantDTO)

        assertEquals(imageDTO.uid, result?.uid)
        assertEquals(imageDTO.src, result?.src)
        verify { imageRepository.saveImage(any()) }
    }

    @Test
    fun `saveImage should store and save new image when imageFile is provided`() {
        val mockFile = mockk<MultipartFile>()

        every { mockFile.originalFilename } returns "new.jpg"
        every { mockFile.inputStream } returns ByteArrayInputStream("data".toByteArray())

        val savedImage = Image(
            id = 2L,
            src = "images/1/Test_123456789.jpg",
            createdAt = OffsetDateTime.now()
        )

        every { imageRepository.saveImage(any()) } returns savedImage

        val variantDTO = VariantDTO(
            productId = 1L,
            title = "Test",
            imageFile = mockFile
        )

        val result = imageService.saveImage(variantDTO)

        assertNotNull(result?.uid)
        assertNotNull(result?.src)
        assertTrue(result?.src?.startsWith("images/1/") ?: false)
        verify { imageRepository.saveImage(any()) }
    }

    @Test
    fun `saveImage should return null when no image is provided`() {
        val variantDTO = VariantDTO(
            productId = 1L,
            title = "Test",
            featuredImage = null,
            imageFile = null
        )

        val result = imageService.saveImage(variantDTO)

        assertNull(result)
        verify(exactly = 0) { imageRepository.saveImage(any()) }
    }

    // findImageById tests
    @Test
    fun `findImageById should return ImageDTO when image exists`() {
        val image = Image(
            id = 1L,
            src = "images/test.jpg",
            createdAt = OffsetDateTime.now()
        )

        every { imageRepository.findImageById(1L) } returns image

        val result = imageService.findImageById(1L)

        assertNotNull(result)
        assertEquals(1L, result?.uid)
        assertEquals("images/test.jpg", result?.src)
        verify { imageRepository.findImageById(1L) }
    }

    @Test
    fun `findImageById should return null when image doesn't exist`() {
        every { imageRepository.findImageById(999L) } returns null

        val result = imageService.findImageById(999L)

        assertNull(result)
        verify { imageRepository.findImageById(999L) }
    }


    // findImageByExternalId tests
    @Test
    fun `findImageByExternalId should return ImageDTO when image exists`() {
        val image = Image(
            id = 1L,
            src = "images/test.jpg",
            externalId = 100L,
            createdAt = OffsetDateTime.now()
        )

        every { imageRepository.findImageByExternalId(100L) } returns image

        val result = imageService.findImageByExternalId(100L)

        assertNotNull(result)
        assertEquals(100L, result?.externalId)
        verify { imageRepository.findImageByExternalId(100L) }
    }

    @Test
    fun `findImageByExternalId should return null when image doesn't exist`() {
        every { imageRepository.findImageByExternalId(999L) } returns null

        val result = imageService.findImageByExternalId(999L)

        assertNull(result)
        verify { imageRepository.findImageByExternalId(999L) }
    }

    // deleteImage tests
    @Test
    fun `deleteImage should call repository deleteImage`() {
        every { imageRepository.deleteImage(1L) } just Runs

        imageService.deleteImage(1L)

        verify { imageRepository.deleteImage(1L) }
    }
}
