package dev.js.productsdemo.controllers

import dev.js.productsdemo.model.ImageDTO
import dev.js.productsdemo.model.ProductDTO
import dev.js.productsdemo.model.ProductRequest
import dev.js.productsdemo.model.VariantDTO
import dev.js.productsdemo.repository.ImageRepository
import dev.js.productsdemo.service.ProductService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import java.io.File
import java.time.Instant

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
        model.addAttribute("newProduct", ProductRequest(
            product = ProductDTO(variants =
                mutableListOf(VariantDTO(
                    featuredImage = ImageDTO()
                ))
            )
        ))

        return "fragments/product-table :: product-table"
    }

    @PostMapping("/variant-row")
    fun getVariantRow(@ModelAttribute newProduct: ProductRequest, model: Model): String {
        logger.info("Adding New Variant Row: {}", newProduct)
        newProduct.product.variants.add(VariantDTO(featuredImage = ImageDTO()))
        model.addAttribute("variantIndex", newProduct.product.variants.size - 1)
        model.addAttribute("newProduct", newProduct)

        return "fragments/add-variant :: variant-row"
    }

//
//    @RequestParam title: String,
//    @RequestParam(required = false) vendor: String?,
//    @RequestParam(required = false) productType: String?,
//    @RequestParam(name = "pageSize", required = false) pageSize: Int?,

    @PostMapping("")
    fun addProduct(
        @ModelAttribute request: ProductRequest,
        model: Model
    ): String {
        logger.info("Adding product: {}", request)

        val savedProduct = productService.saveProduct(productDTO = request.product)
        logger.info("Saved product: {}", savedProduct)
        // Return updated table
        val productsWithDetails = productService.getAllProductsWithDetails(0, request.pageSize)

        model.addAttribute("productsWithDetails", productsWithDetails)
        return "fragments/product-table"
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
                variants = mutableListOf(
                    VariantDTO(featuredImage = ImageDTO())
                )
            ),
            pageSize = 10
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