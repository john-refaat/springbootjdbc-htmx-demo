package dev.js.productsdemo.service

import dev.js.productsdemo.exceptions.UniqueViolationException
import dev.js.productsdemo.exceptions.VariantNotFoundException
import dev.js.productsdemo.mappers.toImage
import dev.js.productsdemo.mappers.toVariant
import dev.js.productsdemo.model.ImageDTO
import dev.js.productsdemo.model.VariantDTO
import dev.js.productsdemo.repository.VariantRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.multipart.MultipartFile
import java.math.BigDecimal
import java.time.OffsetDateTime

class VariantServiceImplTest {

    private lateinit var variantService: VariantService
    private lateinit var imageService: ImageService
    private lateinit var variantRepository: VariantRepository


    private lateinit var imageDTO1: ImageDTO
    private lateinit var imageDTO2: ImageDTO

    private lateinit var variantDTO1: VariantDTO
    private lateinit var variantDTO2: VariantDTO

    @BeforeEach
    fun setUp() {
        variantRepository = mockk()
        imageService = mockk()
        variantService = VariantServiceImpl(variantRepository, imageService)

        imageDTO1 = ImageDTO(
            uid = 1L,
            src = "images/test.jpg",
            externalId = 100L,
            createdAt = OffsetDateTime.now()
        )
        imageDTO2 = ImageDTO(
            uid = 2L,
            src = "images/test2.jpg",
            externalId = 101L,
            createdAt = OffsetDateTime.now()
        )
        variantDTO1 = VariantDTO(
            productId = 1L,
            title = "Red/Test 1",
            sku = "test-sku-1",
            price = BigDecimal("10.99"),
            available = true,
            externalId = 1234567890,
            createdAt = OffsetDateTime.now(),
            option1 = "Red"
        )
        variantDTO2 = VariantDTO(
            productId = 1L,
            title = "Blue/Test 2",
            sku = "test-sku-2",
            price = BigDecimal("10.99"),
            available = true,
            externalId = 1234567891,
            createdAt = OffsetDateTime.now(),
            option1 = "Blue"
        )
    }

    @Test
    fun `saveVariant should save and return variant even when image is not provided`() {

        every { variantRepository.externalIdExists(any()) } returns false
        every { imageService.saveImage(any()) } returns null
        every { variantRepository.saveOrUpdateVariant(any()) } returns variantDTO1.toVariant()
        val savedVariant = variantService.saveVariant(variantDTO1)
        assertNotNull(savedVariant)
        assertEquals(variantDTO1.productId, savedVariant?.productId)
        assertEquals(variantDTO1.title, savedVariant?.title)
        assertNull(savedVariant?.featuredImage)
        assertNull(savedVariant?.imageFile)
    }

    @Test
    fun `saveVariant should save and return variant when image File is provided`() {

        val imageFile = mockk<MultipartFile>()
        every { variantRepository.externalIdExists(any()) } returns false
        every { imageService.findImageByExternalId(any()) } returns null
        every { imageService.saveImage(any()) } returns imageDTO1
        every { variantRepository.saveOrUpdateVariant(any()) } returns
                variantDTO1.copy(featuredImage = imageDTO1).toVariant()

        val savedVariant = variantService.saveVariant(variantDTO1.copy(imageFile = imageFile))
        assertNotNull(savedVariant)
        assertEquals(variantDTO1.productId, savedVariant?.productId)
        assertEquals(variantDTO1.title, savedVariant?.title)
        assertEquals(imageDTO1.uid, savedVariant?.featuredImage?.uid)
        assertEquals(imageDTO1.src, savedVariant?.featuredImage?.src)
        assertEquals(imageDTO1.externalId, savedVariant?.featuredImage?.externalId)
        assertNotNull(savedVariant?.featuredImage?.createdAt)
        assertNull(savedVariant?.imageFile)

        verify(exactly = 0) { imageService.findImageByExternalId(any()) }
        verify(exactly = 1) { imageService.saveImage(match { it.title == variantDTO1.title
        && it.externalId == variantDTO1.externalId
                && it.imageFile == imageFile}) }
        verify(exactly = 1) {variantRepository.saveOrUpdateVariant(match { it.featuredImage == imageDTO1.toImage() })}
    }

    @Test
    fun `saveVariant should save and return variant when featured image is provided`() {

        val variantDTO = variantDTO1.copy(featuredImage = imageDTO1)

        every { variantRepository.externalIdExists(any()) } returns false
        every { imageService.findImageByExternalId(any()) } returns null
        every { imageService.saveImage(any()) } returns imageDTO1
        every { variantRepository.saveOrUpdateVariant(any()) } returns variantDTO.toVariant()
        val savedVariant = variantService.saveVariant(variantDTO)
        assertNotNull(savedVariant)
        assertEquals(variantDTO.productId, savedVariant?.productId)
        assertEquals(variantDTO.title, savedVariant?.title)
        assertEquals(imageDTO1.uid, savedVariant?.featuredImage?.uid)
        assertEquals(imageDTO1.src, savedVariant?.featuredImage?.src)
        assertEquals(imageDTO1.externalId, savedVariant?.featuredImage?.externalId)
        assertNotNull(savedVariant?.featuredImage?.createdAt)
        assertNull(savedVariant?.imageFile)
    }

    @Test
    fun `saveVariant should throw exception when variant with same externalId already exists`() {
        val variantDTO = VariantDTO(
            productId = 1L,
            sku = "test-sku",
            externalId = 1234567890,
            title = "Test",
            featuredImage = null,
            imageFile = null
        )
        every { variantRepository.externalIdExists(any()) } returns true
        assertThrows(UniqueViolationException::class.java) {
            variantService.saveVariant(variantDTO)
        }
    }

    @Test
    fun `saveVariants should save and return variants`() {
        every { variantRepository.externalIdExists(any()) } returns false
        every { imageService.findImageByExternalId(any()) } returns null
        every { imageService.saveImage(any()) } returnsMany listOf(imageDTO1, imageDTO2)
        every { variantRepository.saveOrUpdateVariant(any()) } returnsMany listOf(variantDTO1.toVariant(),
            variantDTO2.toVariant())

        val variants = listOf(variantDTO1, variantDTO2)
        val savedVariants = variantService.saveVariants(variants)
        assertEquals(variants.size, savedVariants.size)
        assertEquals(variants[0].productId, savedVariants[0].productId)
        assertEquals(variants[0].title, savedVariants[0].title)
        assertEquals(variants[1].productId, savedVariants[1].productId)
        assertEquals(variants[1].title, savedVariants[1].title)
    }

    @Test
    fun `saveVariants should throw exception when 2 variants have same externalId`() {
        val variantDTO = variantDTO1.copy(externalId = variantDTO2.externalId)
        assertThrows(UniqueViolationException::class.java) {
            variantService.saveVariants(listOf(variantDTO, variantDTO2))
        }
    }

    @Test
    fun `saveVariants should throw exception when 2 variants have same title`() {
        val variantDTO = variantDTO1.copy(title = variantDTO2.title)
        assertThrows(UniqueViolationException::class.java) {
            variantService.saveVariants(listOf(variantDTO, variantDTO2))
        }
    }

    @Test
    fun `saveVariants should throw exception when 2 variants have same sku`() {
        val variantDTO = variantDTO1.copy(sku = variantDTO2.sku)
        assertThrows(UniqueViolationException::class.java) {
            variantService.saveVariants(listOf(variantDTO, variantDTO2))
        }
    }

    @Test
    fun `findVariantById should return variant`() {
        val id = 1234L
        val variant = variantDTO1.toVariant().copy(id = id)
        every { variantRepository.findVariantById(any()) } returns variant
        val result = variantService.findVariantById(variant.id!!)
        assertNotNull(result)
        assertEquals(id, result?.uid)
        assertEquals(variantDTO1.title, result?.title)
        assertEquals(variantDTO1.sku, result?.sku)
        assertEquals(variantDTO1.price, result?.price)
        assertEquals(variantDTO1.available, result?.available)
        assertEquals(variantDTO1.externalId, result?.externalId)
    }

    @Test
    fun `findVariantById should throw exception when variant not found`() {
        every { variantRepository.findVariantById(any()) } returns null
        assertThrows(VariantNotFoundException::class.java) {
            variantService.findVariantById(1234L)
        }
    }

    @Test
    fun `findVariantByIdAndProductId should return variant`() {
        val id = 1234L
        val variant = variantDTO1.toVariant().copy(id = id)
        every { variantRepository.findVariantByIdAndProductId(any(), any()) } returns variant
        val result = variantService.findVariantByIdAndProductId(variant.id!!, variant.productId!!)
        assertNotNull(result)
        assertEquals(id, result?.uid)
        assertEquals(variantDTO1.title, result?.title)
        assertEquals(variantDTO1.sku, result?.sku)
        assertEquals(variantDTO1.price, result?.price)
    }

    @Test
    fun `findVariantByIdAndProductId should throw exception when variant not found`() {
        every { variantRepository.findVariantByIdAndProductId(any(), any()) } returns null
        assertThrows(VariantNotFoundException::class.java) {
            variantService.findVariantByIdAndProductId(1234L, 1234L)
        }
    }

    @Test
    fun `updateVariant should update variant`() {
        val id = 1234L
        val variant = variantDTO1.toVariant().copy(id = id)
        val updatedVariantDTO = variantDTO1.copy(title = "Updated Title")

        every { variantRepository.findVariantById(id) } returns variant
        every { variantRepository.saveOrUpdateVariant(any()) } returns variant.copy(title = "Updated Title")

        val updatedVariant = variantService.updateVariant(variant.id!!, updatedVariantDTO)

        verify(exactly = 1) { variantRepository.findVariantById(eq(id)) }
        verify(exactly = 1) { variantRepository.saveOrUpdateVariant(match {
            it.id == id &&
            it.title == "Updated Title" &&
            it.productId == variant.productId &&
            it.sku == variant.sku &&
            it.price == variant.price &&
            it.available == variant.available &&
            it.externalId == variant.externalId
        }) }


        assertNotNull(updatedVariant)
        assertEquals(variant.id, updatedVariant?.uid)
        assertEquals("Updated Title", updatedVariant?.title)
        assertEquals(variant.productId, updatedVariant?.productId)
    }

    @Test
    fun `updateVariant should throw exception when variant not found`() {
        val id = 1234L
        val variant = variantDTO1.toVariant().copy(id = id)
        val updatedVariantDTO = variantDTO1.copy(title = "Updated Title")
        every { variantRepository.findVariantById(any()) } returns null
        assertThrows(VariantNotFoundException::class.java) {
            variantService.updateVariant(variant.id!!, updatedVariantDTO)
        }
        verify(exactly = 1) { variantRepository.findVariantById(eq(id)) }
        verify(exactly = 0) { variantRepository.saveOrUpdateVariant(any()) }
    }

}