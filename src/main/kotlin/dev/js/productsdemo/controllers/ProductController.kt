package dev.js.productsdemo.controllers

import dev.js.productsdemo.model.ImageDTO
import dev.js.productsdemo.model.ProductDTO
import dev.js.productsdemo.model.ProductRequest
import dev.js.productsdemo.model.VariantDTO
import dev.js.productsdemo.service.ProductService
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
    private val logger = LoggerFactory.getLogger(ProductController::class.java)

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

   /* @PostMapping("/variant-row")
    fun getVariantRow(@ModelAttribute("newProduct") newProduct: ProductRequest, model: Model): String {
        logger.info("Adding New Variant Row: {}", newProduct)
        newProduct.variantCount += 1
        newProduct.product.variants = listOf(
            VariantDTO(featuredImage = ImageDTO()),
            VariantDTO(featuredImage = ImageDTO()),
            VariantDTO(featuredImage = ImageDTO()))
        model.addAttribute("variantIndex", newProduct.variantCount - 1)
        model.addAttribute("variantCount", newProduct.variantCount)
        return "fragments/add-variant :: variant-row"
    }
*/
//
//    @RequestParam title: String,
//    @RequestParam(required = false) vendor: String?,
//    @RequestParam(required = false) productType: String?,
//    @RequestParam(name = "pageSize", required = false) pageSize: Int?,

    @PostMapping("")
    fun addProduct(
        @Valid @ModelAttribute("newProduct") newProduct: ProductRequest,
        bindingResult: BindingResult,
        @RequestParam("variantVisible[0]", required = false, defaultValue = "true") visible0: Boolean,
        @RequestParam("variantVisible[1]", required = false, defaultValue = "false") visible1: Boolean,
        @RequestParam("variantVisible[2]", required = false, defaultValue = "false") visible2: Boolean,
        model: Model,
        response: HttpServletResponse  // Add this
    ): String {
        if (bindingResult.hasErrors()) {
            response.status = HttpServletResponse.SC_BAD_REQUEST

            // Pass visibility state back to the view
            val visibleVariants = listOf(visible0, visible1, visible2)
            model.addAttribute("visibleVariants", visibleVariants)

            return "fragments/form :: product-form"
        }

        logger.info("Adding product: {}", newProduct)
        // Process only visible variants
        val visibleVariants = listOf(visible0, visible1, visible2)
        val activeVariants = newProduct.product.variants.filterIndexed { index, _ ->
            index < 3 && visibleVariants.getOrElse(index) { false }
        }
        val savedProduct = productService.saveProduct(productDTO = newProduct.product.copy(variants = activeVariants))
        logger.info("Saved product: {}", savedProduct)
        // Return updated table
       // val productsWithDetails = productService.getAllProductsWithDetails(0, newProduct.pageSize)

        //model.addAttribute("productsWithDetails", productsWithDetails)
        model.addAttribute("visibleVariants", listOf(true, false, false))
        model.addAttribute("newProduct", ProductRequest(
            product = ProductDTO(variants = listOf(
                VariantDTO(featuredImage = ImageDTO()),
                VariantDTO(featuredImage = ImageDTO()),
                VariantDTO(featuredImage = ImageDTO()))
            )
        ))
        model.addAttribute("success", true)
        return "fragments/form :: product-form"
    }

}

@Controller
class IndexController {

    @GetMapping("", "/")
    fun index(model: Model): String {
        model.addAttribute("variantImages", listOf<MultipartFile>())
        model.addAttribute("newProduct", ProductRequest(
            product = ProductDTO(
                title = "",
                vendor = "",
                productType = "",
                variants = listOf(
                    VariantDTO(featuredImage = ImageDTO()),
                    VariantDTO(featuredImage = ImageDTO()),
                    VariantDTO(featuredImage = ImageDTO()))
            ),
            pageSize = 5
        ))
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