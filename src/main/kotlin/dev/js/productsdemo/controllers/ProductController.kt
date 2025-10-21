package dev.js.productsdemo.controllers

import dev.js.productsdemo.controllers.ProductController.Companion.getCreateProductForm
import dev.js.productsdemo.model.ImageDTO
import dev.js.productsdemo.model.ProductDTO
import dev.js.productsdemo.model.ProductRequest
import dev.js.productsdemo.model.VariantDTO
import dev.js.productsdemo.service.ProductService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File

@Controller
@RequestMapping("/products")
class ProductController(private val productService: ProductService) {
    companion object {
        private val logger = LoggerFactory.getLogger(ProductController::class.java)
        fun getCreateProductForm(): ProductRequest =
            ProductRequest(ProductDTO(variants = listOf(
                VariantDTO(featuredImage = ImageDTO()),
                VariantDTO(featuredImage = ImageDTO()),
                VariantDTO(featuredImage = ImageDTO())
            )))
    }


    @GetMapping("")
    fun loadProducts(
        @RequestParam(required = false) currentPage: Int?,
        @RequestParam(required = false) pageSize: Int?,
        model: Model
    ): String {
        logger.info("Loading products with page: {}, size: {}", currentPage, pageSize)
        val productsWithDetails = productService.getAllProductsWithDetails(currentPage?:0, pageSize)
        model.addAttribute("productsWithDetails", productsWithDetails)
        return "fragments/product-table :: product-table"
    }

    @PostMapping(value = ["", "{uid}"])
    fun saveOrUpdateProduct(
        @PathVariable(required = false) uid: Long?,
        @Valid @ModelAttribute("newProduct") newProduct: ProductRequest,
        bindingResult: BindingResult,
        @RequestParam("variantVisible[0]", required = false) visible0: Boolean,
        @RequestParam("variantVisible[1]", required = false) visible1: Boolean,
        @RequestParam("variantVisible[2]", required = false) visible2: Boolean,
        model: Model,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): String {
        if (bindingResult.hasErrors()) {
            response.status = HttpServletResponse.SC_BAD_REQUEST

            val visibleVariants = listOf(visible0, visible1, visible2)
            model.addAttribute("visibleVariants", visibleVariants)

            while (newProduct.product.variants.size < 3) {
                newProduct.product.variants += VariantDTO(featuredImage = ImageDTO())
            }
            return "fragments/form :: product-form"
        }

        try {
            val isUpdate = uid != null
            logger.info("{} product{}", if (isUpdate) "Updating" else "Adding", if (isUpdate) " with uid: $uid" else "")

            // Process only visible variants
            val visibleVariants = listOf(visible0, visible1, visible2)
            val activeVariants = newProduct.product.variants.filterIndexed { index, _ ->
                index < 3 && visibleVariants.getOrElse(index) { false }
            }

            val savedProduct = if (isUpdate) {
                productService.updateProduct(
                    uid = uid!!,
                    productDTO = newProduct.product.copy(uid = uid, variants = activeVariants)
                )
            } else {
                productService.saveProduct(productDTO = newProduct.product.copy(variants = activeVariants))
            }

            logger.info("{} product: {}", if (isUpdate) "Updated" else "Saved", savedProduct)

            model.addAttribute("visibleVariants", listOf(false, false, false))
            model.addAttribute("newProduct", getCreateProductForm())
            model.addAttribute("success", true)
            model.addAttribute("successMessage", if (isUpdate) "Product updated successfully!" else "Product added successfully!")
            return "fragments/form :: product-form"

        } catch (e: Exception) {
            while (newProduct.product.variants.size < 3) {
                newProduct.product.variants += VariantDTO(featuredImage = ImageDTO())
            }
            request.setAttribute("newProduct", newProduct)
            request.setAttribute("visibleVariants", listOf(visible0, visible1, visible2))
            throw e
        }
    }

    @GetMapping("edit/{uid}")
    fun editProduct(@PathVariable("uid") uid: Long, model: Model): String {
        logger.info("Editing product with uid: {}", uid)
        val product = productService.getProductById(uid)
        model.addAttribute("newProduct", ProductRequest(product, mode="edit"))
        return "fragments/form :: product-form"
    }

}

@Controller
class IndexController {

    @GetMapping("", "/")
    fun index(model: Model): String {
        model.addAttribute("variantImages", listOf<MultipartFile>())
        model.addAttribute("newProduct", getCreateProductForm())
        return "index"
    }

}

@Controller
class ImageController(
    @Value("\${app.upload.dir}") private val uploadDir: String,
) {
    @GetMapping("/images/{productId}/{variantId}")
    @ResponseBody
    fun serveFile(@PathVariable productId: String, @PathVariable variantId: String): ByteArray {
        val productDir = File("$uploadDir/$productId")
        val variantFile = productDir.listFiles { file ->
            file.name.startsWith(variantId)
        }?.firstOrNull() ?: throw RuntimeException("Image not found")

        return variantFile.readBytes()
    }
}